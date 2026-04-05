package org.biotech.ui.animation;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;

/**
 * UI动画接口 - 定义可复用动画组件的标准契约
 *
 * <p><b>设计规范：</b></p>
 * <ul>
 *   <li>动画实例应声明为接口类型 {@code IBaseAnimation}，而非具体实现类</li>
 *   <li>动画实例应在构造函数中初始化，支持运行时替换不同动画实现</li>
 *   <li>动画更新通过 TICK 事件监听：{@code addEventListener(UIEvents.TICK, event -> animation.tick())}</li>
 *   <li>动画应用在渲染方法中调用：{@code animation.animation(context, scale)}</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * public class MySlot extends ItemSlot {
 *     private IBaseAnimation floatAnimation;  // 声明为接口类型
 *     private float currentScale = 1.0f;
 *
 *     public MySlot() {
 *         super();
 *         this.floatAnimation = new ShowyAnimation(0.5f);  // 构造函数初始化
 *
 *         // TICK事件驱动动画更新
 *         this.addEventListener(UIEvents.TICK, event -> floatAnimation.tick());
 *     }
 *
 *     &#64;Override
 *     protected void drawSlotOverlay(GUIContext context) {
 *         context.pose.pushPose();
 *         floatAnimation.animation(context, currentScale);  // 应用动画
 *         super.drawSlotOverlay(context);
 *         context.pose.popPose();
 *     }
 * }
 * </pre>
 *
 * @see ShowyAnimation
 */
public interface IBaseAnimation {

    /**
     * 应用动画变换到渲染上下文
     *
     * @param context GUI渲染上下文，包含pose矩阵和partialTick
     * @param scale   当前UI缩放比例，动画应根据此值调整幅度
     */
    void animation(GUIContext context, float scale);

    /**
     * 更新动画状态（每tick调用）
     *
     * <p>应在 {@code UIEvents.TICK} 事件监听中调用，实现帧率无关的动画更新</p>
     */
    void tick();
}
