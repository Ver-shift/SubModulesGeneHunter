package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ISafeZoneBorderRenderer {

    void render(Level level, Vec3 cameraPos, PoseStack poseStack);
}
