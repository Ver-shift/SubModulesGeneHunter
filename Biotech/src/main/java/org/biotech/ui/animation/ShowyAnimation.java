package org.biotech.ui.animation;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;

import java.util.Random;

/**
 * 浮动动画 - 使UI元素产生正弦波浮动效果
 * 支持独立控制X轴和Y轴的浮动参数
 */
public class ShowyAnimation implements IBaseAnimation {

    // 动画状态
    private float angleX = 0.0f;
    private float angleY = 0.0f;

    // 配置参数
    private final float speedX;
    private final float speedY;
    private final float phaseX;
    private final float phaseY;
    private final float amplitudeBase;

    /**
     * 使用默认参数创建浮动动画
     */
    public ShowyAnimation() {
        this(0.5f);
    }

    /**
     * 创建浮动动画
     * @param amplitudeBase 基础浮动幅度（像素）
     */
    public ShowyAnimation(float amplitudeBase) {
        this(amplitudeBase, 0.005f, 0.025f);
    }

    /**
     * 创建浮动动画
     * @param amplitudeBase 基础浮动幅度（像素）
     * @param minSpeed 最小速度
     * @param maxSpeed 最大速度
     */
    public ShowyAnimation(float amplitudeBase, float minSpeed, float maxSpeed) {
        Random random = new Random();
        this.amplitudeBase = amplitudeBase;
        this.speedX = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
        this.speedY = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
        this.phaseX = random.nextFloat() * (float) Math.PI * 2;
        this.phaseY = random.nextFloat() * (float) Math.PI * 2;
    }

    /**
     * 更新动画状态（每tick调用）
     */
    @Override
    public void tick() {
        angleX += speedX;
        angleY += speedY;
    }


    @Override
    public void animation(GUIContext context,float scale) {
        applyTransform(context, scale);
    }

    /**
     * 应用动画变换到PoseStack
     * @param context GUI上下文，包含pose和partialTick
     * @param scale 当前缩放比例
     */
    public void applyTransform(GUIContext context, float scale) {
        float[] offset = calculateOffset(context.partialTick, scale);
        context.pose.translate(offset[0], offset[1], 0);
    }

    /**
     * 计算当前浮动偏移量
     * @param partialTick 部分tick，用于插值
     * @param scale 当前缩放比例
     * @return float[0]=offsetX, float[1]=offsetY
     */
    public float[] calculateOffset(float partialTick, float scale) {
        float interpolatedAngleX = angleX + speedX * partialTick;
        float interpolatedAngleY = angleY + speedY * partialTick;

        float amplitude = amplitudeBase * scale;

        float offsetX = (float) Math.sin(interpolatedAngleX + phaseX) * amplitude;
        float offsetY = (float) Math.cos(interpolatedAngleY + phaseY) * amplitude;

        return new float[]{offsetX, offsetY};
    }
}
