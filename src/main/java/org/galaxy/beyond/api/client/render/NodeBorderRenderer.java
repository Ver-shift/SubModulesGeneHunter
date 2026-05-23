package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;

/**
 * Safe_Zone / Node_Zone 边框渲染 —— 连通分量矩形盒子。
 * Active_Zone 由 {@link ActiveZoneBorderRenderer} 单独处理（不规则墙体）。
 */
public class NodeBorderRenderer extends ZoneBorderRenderer {

    private static final float HALF_HEIGHT = 192f;
    private static final int BOX_VERTICES = 16;

    private int lastHash;
    private final List<BoxDraw> boxes = new ArrayList<>();

    private record BoxDraw(double minX, double minZ, float r, float g, float b, float a) {}

    public void render(Level level, Vec3 cameraPos, PoseStack ps) {
        if(true) return;

        if (level == null) return;
        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;
        LevelZoneData zd = dimData.getLevelZoneData();
        if (zd == null || !zd.hasZones()) return;

        Set<Map.Entry<ChunkPos, ZoneType>> entries = zd.getZoneEntries();
        int hash = entries.hashCode();
        if (needsRebuild || hash != lastHash) {
            rebuild(entries);
            lastHash = hash;
            needsRebuild = false;
        }
        for (BoxDraw box : boxes) {
            drawBorder("Node zone", box.r(), box.g(), box.b(), box.a(),
                    box.minX() - cameraPos.x, -cameraPos.y, box.minZ() - cameraPos.z);
        }
    }

    private void rebuild(Set<Map.Entry<ChunkPos, ZoneType>> entries) {
        Map<ZoneType, Set<ChunkPos>> grouped = new TreeMap<>(Comparator.comparing(ZoneType::ordinal));
        for (var e : entries)
            if (e.getValue() == ZoneType.Node_Zone) // 只画节点区
                grouped.computeIfAbsent(e.getValue(), k -> new HashSet<>()).add(e.getKey());

        boxes.clear();
        List<ZoneHelper.Bounds> allBounds = new ArrayList<>();
        for (var ge : grouped.entrySet()) {
            for (var comp : ZoneHelper.findConnectedComponents(ge.getValue())) {
                ZoneHelper.Bounds b = ZoneHelper.boundsOf(comp);
                allBounds.add(b);
                float[] c = colorFor(ge.getKey());
                boxes.add(new BoxDraw(b.minX() * 16.0, b.minZ() * 16.0, c[0], c[1], c[2], c[3]));
            }
        }

        int totalVerts = allBounds.size() * BOX_VERTICES;
        if (totalVerts == 0) return;
        ensureBuffer(totalVerts);

        try (ByteBufferBuilder bb = ByteBufferBuilder.exactlySized(totalVerts * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            for (var b : allBounds) {
                float w = (float)((b.maxX() + 1) * 16.0 - b.minX() * 16.0);
                float d = (float)((b.maxZ() + 1) * 16.0 - b.minZ() * 16.0);
                RenderHelper.writeBoxWalls(builder, w, d, HALF_HEIGHT);
            }
            try (MeshData md = builder.buildOrThrow()) {
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.vertexBuffer.slice(), md.vertexBuffer());
            }
        }
        indexCount = allBounds.size() * 24;
    }

    private static float[] colorFor(ZoneType t) {
        return switch (t) {
            case Safe_Zone -> new float[]{64/255f, 120/255f, 220/255f, 0.55f};
            case Node_Zone -> new float[]{255/255f, 165/255f, 0/255f, 0.55f};
            default -> new float[]{180/255f, 180/255f, 180/255f, 0.55f};
        };
    }

    @Override protected void rebuildBuffer() {}
}
