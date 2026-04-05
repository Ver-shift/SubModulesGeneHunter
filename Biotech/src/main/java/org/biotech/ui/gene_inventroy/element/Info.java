package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.biotech.ui.IScalable;

import java.util.ArrayList;
import java.util.List;

/**
 * 统计整个存储的技能信息
 */
public class Info extends Label implements IScalable {

    public static final int baseHeight = 50;
    public static final int baseFontSize = 9;
    public static final int baseLabelHeight = 9;

    // 浅绿色文本颜色 #94af60
    private static final int TEXT_COLOR = 0x94af60;
    // 每 tick 自动下滚的像素速度（仅在 hover 时生效）
    private static final float AUTO_SCROLL_PIXEL_PER_TICK = 0.6f;
    private static final float DEFAULT_SKEW_ANGLE = 28.44f;

    private final List<MutableComponent> components = new ArrayList<>();
    private final List<InfoLabel> infoLabels = new ArrayList<>();

    private final ScrollerView scrollerView;
    private final UIElement infoContainer;

    private float currentScale = 1f;
    private float skewAngle = DEFAULT_SKEW_ANGLE;
    private float maxSkewPadding = 0f;

    public Info() {
        super();
        this.layout(layout -> {
            layout.widthPercent(100f);
            layout.height(baseHeight);
        });
        this.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        this.setText(Component.empty());
        this.scrollerView = new ScrollerView();
        this.scrollerView.layout(layout -> {
            layout.widthPercent(100f);
            layout.heightPercent(100f);
        });
        this.scrollerView.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        this.scrollerView.scrollerStyle(style -> style
                .mode(ScrollerMode.VERTICAL)
                .verticalScrollDisplay(ScrollDisplay.NEVER)
                .horizontalScrollDisplay(ScrollDisplay.NEVER)
        );
        // 强制隐藏内置滚动条与按钮，仅保留程序控制的内容偏移。
        this.scrollerView.verticalScroller.setDisplay(false).setAllowHitTest(false);
        this.scrollerView.horizontalScroller.setDisplay(false).setAllowHitTest(false);
        this.scrollerView.viewPort.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        this.scrollerView.viewPort.layout(layout -> layout.paddingAll(0));

        // 内容容器：只放文本行，由 ScrollerView 负责裁剪可视区域。
        this.infoContainer = new UIElement();
        this.infoContainer.layout(layout -> {
            layout.widthPercent(100f);
            layout.flexDirection(FlexDirection.COLUMN);
        });
        this.infoContainer.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        this.scrollerView.addScrollViewChild(infoContainer);

        // 禁用滚轮手动滚动，只保留“悬停自动向下滚”。
        this.scrollerView.viewPort.addEventListener(UIEvents.MOUSE_WHEEL, UIEvent::stopPropagation, true);

        this.addChild(scrollerView);
        this.addEventListener(UIEvents.TICK, event -> autoScrollWhenHovered());
    }

    public void updateInfo(List<MutableComponent> components) {
        this.components.clear();
        this.components.addAll(components);

        this.infoContainer.clearAllChildren();
        this.infoLabels.clear();

        for (MutableComponent component : this.components) {
            InfoLabel infoLabel = new InfoLabel(component, currentScale);
            this.infoLabels.add(infoLabel);
            this.infoContainer.addChild(infoLabel);
        }

        applySkewLayout();

        // 每次刷新文本都回到顶部。
        this.scrollerView.verticalScroller.setValue(0f, false);
    }

    public void setSkewAngle(float skewAngle) {
        this.skewAngle = skewAngle;
        applySkewLayout();
    }

    public void setMaxSkewPadding(float maxSkewPadding) {
        this.maxSkewPadding = Math.max(0f, maxSkewPadding);
        applySkewLayout();
    }

    private void applySkewLayout() {
        if (infoLabels.isEmpty()) {
            return;
        }

        float radians = (float) Math.toRadians(skewAngle);
        float step = Math.abs((float) Math.tan(radians)) * (baseLabelHeight * currentScale);
        for (int i = 0; i < infoLabels.size(); i++) {
            // Align first row to imageEmpty, then shift rows toward left as they go downward.
            float offset = Math.max(0f, maxSkewPadding - (i * step));
            infoLabels.get(i).applyIndent(offset);
        }
    }

    private void autoScrollWhenHovered() {
        if (!this.scrollerView.isSelfOrChildHover()) {
            return;
        }

        var overflowHeight = Math.max(0f, scrollerView.getContainerHeight() - scrollerView.viewPort.getContentHeight());
        if (overflowHeight <= 0f) {
            return;
        }

        float currentValue = scrollerView.verticalScroller.getValue();
        if (currentValue >= 1f) {
            return;
        }

        var deltaNormalized = AUTO_SCROLL_PIXEL_PER_TICK / overflowHeight;
        scrollerView.verticalScroller.setValue(Math.min(1f, currentValue + deltaNormalized));
    }

    @Override
    public void scale(float scale) {
        this.currentScale = scale;
        this.layout(layout -> layout.height(baseHeight * scale));

        for (InfoLabel infoLabel : infoLabels) {
            infoLabel.applyScale(scale);
        }
        applySkewLayout();
    }

    public class InfoLabel extends UIElement {
        private final UIElement spacer;
        private final Label label;

        public InfoLabel(MutableComponent component, float scale) {
            this.layout(layout -> {
                layout.widthPercent(100f);
                layout.flexDirection(FlexDirection.ROW);
            });
            this.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));

            this.spacer = new UIElement();
            this.spacer.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
            this.spacer.layout(layout -> layout.width(0));

            this.label = new Label();
            this.label.setText(component);
            this.label.layout(layout -> {
                layout.widthPercent(100f);
                layout.height(baseLabelHeight * scale);
            });
            this.label.textStyle(style -> {
                style.textWrap(TextWrap.HIDE);
                style.textColor(TEXT_COLOR);
                style.fontSize(baseFontSize * scale);
            });
            this.label.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));

            this.addChildren(spacer, label);
        }

        public void applyScale(float scale) {
            this.label.layout(layout -> layout.height(baseLabelHeight * scale));
            this.label.textStyle(style -> style.fontSize(baseFontSize * scale));
        }

        public void applyIndent(float indent) {
            this.spacer.layout(layout -> layout.width(indent));
        }
    }
}
