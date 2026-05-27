package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.Set;

public class ActiveZoneBorderRenderer extends ZoneBorderRenderer {
    @Override
    public void render(Level level, Camera camera, PoseStack poseStack) {
        if (level == null) return;
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data == null || !data.hasZones()) return;

        Set<ChunkPos> activeChunks = data.activeChunks();
        if (activeChunks.isEmpty()) return;

        for (Set<ChunkPos> component : ZoneHelper.findConnectedComponents(activeChunks)) {
            ZoneHelper.Bounds bounds = ZoneHelper.boundsOf(component);
            RenderHelper.renderBorder(
                    bounds.minX() * 16.0,
                    bounds.minZ() * 16.0,
                    (bounds.maxX() + 1) * 16.0,
                    (bounds.maxZ() + 1) * 16.0,
                    220, 220, 220,
                    100, 25,
                    camera, poseStack
            );
        }
    }
}
