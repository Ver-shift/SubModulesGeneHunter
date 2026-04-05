package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import lombok.Getter;
import lombok.Setter;


/**
 * 保持整体比例不变，进行全屏幕填充
 * 类似于 CSS 的 object-fit: contain
 */
@Getter
@Setter
public class BaseRoot extends UIElement {

    private UIAnimationState uiState = UIAnimationState.PRE;


    private float baseWidth = 280f;
    private float baseHeight = 188f;
    private float currentScale = 1f;
    private boolean scaleDirty = false;  // 脏数据标记：scale 是否已变化但未消费
    private float lastParentWidth = -1;
    private float lastParentHeight = -1;

    public BaseRoot() {
        this.addEventListener(UIEvents.TICK, event -> {
            checkParentSizeChanged();
            scaleTick();
        });
    }

    protected void scaleTick(){};

    /**
     * 检测父容器尺寸是否变化
     */
    private void checkParentSizeChanged() {
        UIElement parent = this.getParent();
        if (parent == null) return;
        
        float width = parent.getSizeWidth();
        float height = parent.getSizeHeight();
        
        if (width != lastParentWidth || height != lastParentHeight) {
            lastParentWidth = width;
            lastParentHeight = height;
            updateScale();
        }
    }

    /**
     * 计算长宽比例
     */
    public float getAspectRatio() {
        return baseWidth / baseHeight;
    }

    /**
     * 计算整体缩放比例，保持宽高比填充容器
     */
    public float calculateScaleFactor(float containerWidth, float containerHeight) {
        float containerAspect = containerWidth / containerHeight;
        float contentAspect = getAspectRatio();

        if (containerAspect > contentAspect) {
            // 容器更宽，以高度为准
            return containerHeight / baseHeight;
        } else {
            // 容器更高，以宽度为准
            return containerWidth / baseWidth;
        }
    }

    /**
     * 更新缩放
     */
    private void updateScale() {
        UIElement parent = this.getParent();
        if (parent == null) return;
        
        float containerWidth = parent.getSizeWidth();
        float containerHeight = parent.getSizeHeight();
        
        if (containerWidth <= 0 || containerHeight <= 0) return;

        float newScale = calculateScaleFactor(containerWidth, containerHeight);
        
        // 只有 scale 真正变化时才更新并标记为脏数据
        if (newScale != currentScale) {
            currentScale = newScale;
            scaleDirty = true;  // 标记为脏数据
        }
        
        // 设置自身尺寸为缩放后的尺寸
        float scaledWidth = baseWidth * currentScale;
        float scaledHeight = baseHeight * currentScale;
        this.layout(layout -> {
            layout.width((int) scaledWidth);
            layout.height((int) scaledHeight);
        });
    }


    /**
     * 获取当前 scale 值，并清除脏数据标记
     * 在子类的 screenTick() 中调用此方法获取 scale，会自动标记为干净数据
     */
    public float getCurrentScale() {
        scaleDirty = false;  // 消费脏数据，标记为干净
        return currentScale;
    }

    /**
     * 检测 scale 是否为脏数据（已变化但未消费）
     */
    protected boolean isScaleDirty() {
        return scaleDirty;
    }



    /**
     * ui 的阶段，用来处理动画
     */
    public enum UIAnimationState {
        /**
         * 入场动画
         */
        PRE,

        /**
         * 静止状态
         */
        ON,

        /**
         * 退场动画
         */
        POST
    }
}
