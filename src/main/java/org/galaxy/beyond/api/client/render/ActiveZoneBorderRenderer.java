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
 * 不规则活跃区域渲染 —— 逐区块暴露面，形成凹凸轮廓。
 */
public class ActiveZoneBorderRenderer extends ZoneBorderRenderer {

    private static final float R = 220f / 255f, G = 220f / 255f, B = 220f / 255f, A = 0.35f;

    private int lastHash;

    public void render(Level level, Vec3 cameraPos, PoseStack ps) {
        if (level == null) return;
        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;
        LevelZoneData zd = dimData.getLevelZoneData();
        if (zd == null || !zd.hasZones()) return;

        var active = ZoneHelper.filterByMask(zd.getZoneEntries(), ZoneType.Active_Zone.mask());
        if (active.isEmpty()) return;

        Set<ChunkPos> activeSet = active.chunks();
        int hash = activeSet.hashCode();
        if (needsRebuild || hash != lastHash) {
            rebuildBuffer(activeSet, (float) level.getMinY(), (float) level.getMaxY());
            lastHash = hash;
            needsRebuild = false;
        }

        drawBorder("Active zone border", R, G, B, A, -cameraPos.x, -cameraPos.y, -cameraPos.z);
    }

    @Override
    protected void rebuildBuffer() {
        // called by render inline
    }

    private void rebuildBuffer(Set<ChunkPos> activeSet, float worldMinY, float worldMaxY) {
        record Wall(float atX, float atZ, float sMin, float sMax, boolean xFixed) {}
        List<Wall> walls = new ArrayList<>();

        for (ChunkPos c : activeSet) {
            int cx = c.getMinBlockX() >> 4, cz = c.getMinBlockZ() >> 4;
            float x0 = cx * 16f, z0 = cz * 16f, x1 = x0 + 16f, z1 = z0 + 16f;

            if (!activeSet.contains(new ChunkPos(cx + 1, cz))) walls.add(new Wall(x1, 0, z0, z1, true));
            if (!activeSet.contains(new ChunkPos(cx - 1, cz))) walls.add(new Wall(x0, 0, z0, z1, true));
            if (!activeSet.contains(new ChunkPos(cx, cz + 1))) walls.add(new Wall(0, z1, x0, x1, false));
            if (!activeSet.contains(new ChunkPos(cx, cz - 1))) walls.add(new Wall(0, z0, x0, x1, false));
        }

        indexCount = walls.size() * 6;
        if (indexCount == 0) return;
        ensureBuffer(walls.size() * 4);

        try (ByteBufferBuilder bb = ByteBufferBuilder.exactlySized(walls.size() * 4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            float bottom = worldMinY + 1f, top = worldMaxY;

            for (Wall w : walls) {
                if (w.xFixed()) {
                    builder.addVertex(w.atX(), bottom, w.sMin()).setUv(0, 0);
                    builder.addVertex(w.atX(), bottom, w.sMax()).setUv(0, 1);
                    builder.addVertex(w.atX(), top, w.sMax()).setUv(1, 1);
                    builder.addVertex(w.atX(), top, w.sMin()).setUv(1, 0);
                } else {
                    builder.addVertex(w.sMin(), bottom, w.atZ()).setUv(0, 0);
                    builder.addVertex(w.sMax(), bottom, w.atZ()).setUv(0, 1);
                    builder.addVertex(w.sMax(), top, w.atZ()).setUv(1, 1);
                    builder.addVertex(w.sMin(), top, w.atZ()).setUv(1, 0);
                }
            }

            try (MeshData md = builder.buildOrThrow()) {
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.vertexBuffer.slice(), md.vertexBuffer());
            }
        }
    }
}
