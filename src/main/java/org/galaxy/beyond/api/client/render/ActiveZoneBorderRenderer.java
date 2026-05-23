package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

import java.util.*;

/**
 * 不规则活跃区域渲染 —— 只画活跃区与节点区/空地的交界，避免安全区边界重叠。
 */
public class ActiveZoneBorderRenderer extends ZoneBorderRenderer {

    private static final float R = 220f/255f, G = 220f/255f, B = 220f/255f, A = 0.35f;

    private int lastHash;

    public void render(Level level, Vec3 cameraPos, PoseStack ps) {
        if (level == null) return;
        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;
        LevelZoneData lzd = dimData.getLevelZoneData();
        if (lzd == null || !lzd.hasZones()) return;

        Set<ChunkPos> activeSet = lzd.activeChunks();
        if (activeSet.isEmpty()) return;

        int hash = activeSet.hashCode();
        if (needsRebuild || hash != lastHash) {
            rebuildBuffer(activeSet, lzd, (float) level.getMinY(), (float) level.getMaxY());
            lastHash = hash;
            needsRebuild = false;
        }

        drawBorder("Active zone", R, G, B, A, -cameraPos.x, -cameraPos.y, -cameraPos.z);
    }

    @Override protected void rebuildBuffer() {}

    private void rebuildBuffer(Set<ChunkPos> activeSet, LevelZoneData lzd, float minY, float maxY) {
        record Wall(float at, float s0, float s1, boolean xFixed) {}
        List<Wall> walls = new ArrayList<>();

        for (ChunkPos c : activeSet) {
            int cx = c.getMinBlockX() >> 4, cz = c.getMinBlockZ() >> 4;
            float x0 = cx * 16f, z0 = cz * 16f, x1 = x0 + 16f, z1 = z0 + 16f;

            ChunkPos e = new ChunkPos(cx + 1, cz), w = new ChunkPos(cx - 1, cz);
            ChunkPos s = new ChunkPos(cx, cz + 1), n = new ChunkPos(cx, cz - 1);

            // 只画与节点区/空地交界（不画与安全区/活跃区的边）
            if (isBorder(e, activeSet, lzd)) walls.add(new Wall(x1, z0, z1, true));
            if (isBorder(w, activeSet, lzd)) walls.add(new Wall(x0, z0, z1, true));
            if (isBorder(s, activeSet, lzd)) walls.add(new Wall(z1, x0, x1, false));
            if (isBorder(n, activeSet, lzd)) walls.add(new Wall(z0, x0, x1, false));
        }

        indexCount = walls.size() * 6;
        if (indexCount == 0) return;
        ensureBuffer(walls.size() * 4);

        try (ByteBufferBuilder bb = ByteBufferBuilder.exactlySized(walls.size() * 4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            float bottom = minY + 1f, top = maxY;

            for (Wall wl : walls) {
                if (wl.xFixed()) {
                    builder.addVertex(wl.at(), bottom, wl.s0()).setUv(0, 0);
                    builder.addVertex(wl.at(), bottom, wl.s1()).setUv(0, 1);
                    builder.addVertex(wl.at(), top, wl.s1()).setUv(1, 1);
                    builder.addVertex(wl.at(), top, wl.s0()).setUv(1, 0);
                } else {
                    builder.addVertex(wl.s0(), bottom, wl.at()).setUv(0, 0);
                    builder.addVertex(wl.s1(), bottom, wl.at()).setUv(0, 1);
                    builder.addVertex(wl.s1(), top, wl.at()).setUv(1, 1);
                    builder.addVertex(wl.s0(), top, wl.at()).setUv(1, 0);
                }
            }

            try (MeshData md = builder.buildOrThrow()) {
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.vertexBuffer.slice(), md.vertexBuffer());
            }
        }
    }

    /** 仅当邻居不是活跃区也不是安全区时画墙（即：空地 或 节点区） */
    private static boolean isBorder(ChunkPos nb, Set<ChunkPos> activeSet, LevelZoneData lzd) {
        if (activeSet.contains(nb)) return false;
        return !lzd.isSafe(nb);
    }
}
