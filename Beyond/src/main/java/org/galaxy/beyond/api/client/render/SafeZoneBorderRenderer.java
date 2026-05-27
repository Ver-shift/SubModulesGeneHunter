package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.Set;

public class SafeZoneBorderRenderer extends ZoneBorderRenderer {
    private static final int BLUE_R = 64;
    private static final int BLUE_G = 120;
    private static final int BLUE_B = 220;
    private static final int ORANGE_R = 220;
    private static final int ORANGE_G = 140;
    private static final int ORANGE_B = 30;
    private static final int RED_R = 200;
    private static final int RED_G = 30;
    private static final int RED_B = 30;

    @Override
    public void render(Level level, Camera camera, PoseStack poseStack) {
        if (level == null) return;
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data == null || !data.hasZones()) return;

        Set<ChunkPos> safeChunks = data.safeChunks();
        if (safeChunks.isEmpty()) return;

        ZoneHelper.Bounds bounds = ZoneHelper.boundsOf(safeChunks);
        int[] color = currentColor();
        RenderHelper.renderBorder(
                bounds.minX() * 16.0,
                bounds.minZ() * 16.0,
                (bounds.maxX() + 1) * 16.0,
                (bounds.maxZ() + 1) * 16.0,
                color[0], color[1], color[2],
                BOTTOM_ALPHA, TOP_ALPHA,
                camera, poseStack
        );
    }

    private static int[] currentColor() {
        var player = Minecraft.getInstance().player;
        if (player == null) return new int[]{BLUE_R, BLUE_G, BLUE_B};

        try {
            PlayerPhase phase = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
            if (phase == PlayerPhase.PRE_ROGUE) return new int[]{ORANGE_R, ORANGE_G, ORANGE_B};
            if (phase != PlayerPhase.LOBBY) return new int[]{RED_R, RED_G, RED_B};
        } catch (Exception ignored) {
        }

        return new int[]{BLUE_R, BLUE_G, BLUE_B};
    }
}
