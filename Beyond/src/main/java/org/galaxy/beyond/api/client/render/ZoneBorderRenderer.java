package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.Level;

public abstract class ZoneBorderRenderer {
    protected static final int BOTTOM_ALPHA = 140;
    protected static final int TOP_ALPHA = 30;

    public abstract void render(Level level, Camera camera, PoseStack poseStack);

    public void invalidate() {
    }
}
