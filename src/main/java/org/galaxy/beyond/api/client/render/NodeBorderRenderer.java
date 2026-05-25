package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

import java.util.*;

/**
 * 节点边框 —— 逐区块暴露面 + 每个节点独立颜色分组渲染。
 */
public class NodeBorderRenderer extends ZoneBorderRenderer {

    private static final float A = 0.45f;

    private int lastHash;
    private final List<ColorGroup> groups = new ArrayList<>();

    private record ColorGroup(float r, float g, float b, GpuBuffer buffer, int indexCount) {}

    public void render(Level level, Vec3 cameraPos, PoseStack ps) {
        if (level == null) return;
        LevelZoneData lzd = BeyondAPI.getLevelZoneData(level);
        if (lzd == null || !lzd.hasZones()) return;

        Set<ChunkPos> nodeSet = lzd.nodeChunks();
        if (nodeSet.isEmpty()) return;

        List<NodeData> nodeDatas = BeyondAPI.getRogueData(level).getNodeDatas();
        int hash = nodeDatas.hashCode();
        if (needsRebuild || hash != lastHash) {
            rebuild(nodeSet, nodeDatas,
                    (float) level.getMinY(), (float) level.getMaxY());
            lastHash = hash;
            needsRebuild = false;
        }

        for (ColorGroup g : groups) {
            this.vertexBuffer = g.buffer();
            this.indexCount = g.indexCount();
            drawBorder("Node zone", g.r(), g.g(), g.b(), A,
                    -cameraPos.x, -cameraPos.y, -cameraPos.z);
        }
    }

    private void rebuild(Set<ChunkPos> nodeSet, List<NodeData> nodeDatas, float minY, float maxY) {
        record Col(float r, float g, float b) {}
        Map<ChunkPos, Col> colorMap = new HashMap<>();
        for (ChunkPos c : nodeSet) {
            for (NodeData nd : nodeDatas) {
                if (!nd.containsChunk(c)) continue;
                colorMap.put(c, switch (nd.getColor()) {
                    case BLUE   -> new Col(64/255f, 120/255f, 220/255f);
                    case GREEN  -> new Col(0/255f, 200/255f, 0/255f);
                    case ORANGE -> new Col(255/255f, 165/255f, 0/255f);
                    case RED    -> new Col(220/255f, 40/255f, 40/255f);
                    default     -> new Col(140/255f, 140/255f, 140/255f);
                });
                break;
            }
        }

        // 按颜色分组收集面片
        record P(float at, float s0, float s1, boolean xF) {}
        Map<Col, List<P>> byColor = new LinkedHashMap<>();

        for (ChunkPos c : nodeSet) {
            int cx = c.getMinBlockX() >> 4, cz = c.getMinBlockZ() >> 4;
            float x0 = cx * 16f, z0 = cz * 16f, x1 = x0 + 16f, z1 = z0 + 16f;
            Col col = colorMap.getOrDefault(c, new Col(140/255f, 140/255f, 140/255f));

            byColor.computeIfAbsent(col, k -> new ArrayList<>());
            var pts = byColor.get(col);
            if (!nodeSet.contains(new ChunkPos(cx + 1, cz))) pts.add(new P(x1, z0, z1, true));
            if (!nodeSet.contains(new ChunkPos(cx - 1, cz))) pts.add(new P(x0, z0, z1, true));
            if (!nodeSet.contains(new ChunkPos(cx, cz + 1))) pts.add(new P(z1, x0, x1, false));
            if (!nodeSet.contains(new ChunkPos(cx, cz - 1))) pts.add(new P(z0, x0, x1, false));
        }

        // 每组颜色独立 buffer
        groups.clear();
        float bottom = minY + 1f, top = maxY;
        int vSize = DefaultVertexFormat.POSITION_TEX.getVertexSize();

        for (var entry : byColor.entrySet()) {
            Col col = entry.getKey();
            var pts = entry.getValue();
            int faceCount = pts.size();
            int vertCount = faceCount * 4;

            // Close old buffer of same color group if any, create new
            GpuBuffer buf = RenderSystem.getDevice()
                    .createBuffer(() -> "Node border " + col, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                            (long) vertCount * vSize);

            try (ByteBufferBuilder bb = ByteBufferBuilder.exactlySized(vertCount * vSize)) {
                BufferBuilder builder = new BufferBuilder(bb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                for (P p : pts) {
                    if (p.xF()) {
                        builder.addVertex(p.at(), bottom, p.s0()).setUv(0, 0);
                        builder.addVertex(p.at(), bottom, p.s1()).setUv(0, 1);
                        builder.addVertex(p.at(), top, p.s1()).setUv(1, 1);
                        builder.addVertex(p.at(), top, p.s0()).setUv(1, 0);
                    } else {
                        builder.addVertex(p.s0(), bottom, p.at()).setUv(0, 0);
                        builder.addVertex(p.s1(), bottom, p.at()).setUv(0, 1);
                        builder.addVertex(p.s1(), top, p.at()).setUv(1, 1);
                        builder.addVertex(p.s0(), top, p.at()).setUv(1, 0);
                    }
                }
                try (MeshData md = builder.buildOrThrow()) {
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buf.slice(), md.vertexBuffer());
                }
            }

            groups.add(new ColorGroup(col.r(), col.g(), col.b(), buf, faceCount * 6));
        }
    }

    @Override protected void rebuildBuffer() {}
}
