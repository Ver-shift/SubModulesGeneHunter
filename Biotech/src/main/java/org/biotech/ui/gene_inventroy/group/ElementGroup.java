package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;

import dev.vfyjxf.taffy.style.TaffyPosition;
import lombok.Getter;
import lombok.Setter;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.gene_inventroy.element.BaseRoot;
import org.biotech.ui.gene_inventroy.element.ToggleButton;

import java.util.HashMap;

/**
 * 存储多个UIelement
 * 能够随时切换，按钮在左上角，内容区域独立
 */
public class ElementGroup extends UIElement {

    private HashMap<String, BaseRoot> children = new HashMap<>();
    private HashMap<String, IGuiTexture> textures = new HashMap<>();
    private HashMap<String, IGuiTexture> hoveredTextures = new HashMap<>();

    @Getter @Setter
    private BaseRoot lastCurrent;
    @Getter @Setter
    private BaseRoot current;

    // 切换按钮
    private final ToggleButton toggleButton;
    private final UIElement toggleElements;

    private final int baseButtonWidth = 25;
    private final int baseButtonHeight = 25;

    public ElementGroup() {
        // 垂直布局：按钮在上，内容在下
        this.layout(layout -> {
            layout.heightPercent(100);
            layout.widthPercent(100);
        });

        // 创建切换按钮（左上角绝对定位）
        toggleButton = new ToggleButton();
        toggleButton.setId("toggle_button");
        toggleButton.noText();
        toggleButton.layout(layout -> {
            layout.width(baseButtonWidth);
            layout.height(baseButtonHeight);
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(2);
            layout.top(2);
        });
        var texture = SpriteTexture
                .of(BiotechTexture.GUI_TEXTURE)
                .setSprite(0,46,25,25);
        var hoverTexture = SpriteTexture
                .of(BiotechTexture.GUI_TEXTURE)
                .setSprite(26,20,25,25);
        toggleButton.buttonStyle(style -> {
            style.baseTexture(texture);
            style.hoverTexture(hoverTexture);
            style.pressedTexture(hoverTexture);
        });
        //整体的组-防止按钮挤占布局（先添加，按钮后添加，这样按钮在最上层）
        toggleElements = new UIElement();
        toggleElements.setId("toggle_elements");
        toggleElements.layout(layout -> {
            layout.heightPercent(100);
            layout.widthPercent(100);
            layout.justifyContent(AlignContent.CENTER);
            layout.alignItems(AlignItems.CENTER);
        });
        this.addChild(toggleElements);

        // 动画配置常量
        final float ANIMATION_DURATION = 0.15f;  // 动画持续时间（秒）
        final float SCALE_DOWN = 0.85f;          // 按下时缩放比例
        
        toggleButton.setOnClick(event -> {
            // 点击时缩小
            toggleButton.animation()
                .duration(ANIMATION_DURATION)
                .ease(Eases.QUART_OUT)
                .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().scale(SCALE_DOWN))
//                .style(PropertyRegistry.COLOR,0x33FFFFFF)   //20% 不透明
                .onFinished(element -> {
                    // 动画完成后弹回原状
                    element.animation()
                        .duration(ANIMATION_DURATION)
                        .ease(Eases.QUART_OUT)
                        .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().scale(1f))
//                        .style(PropertyRegistry.COLOR, 0xFFFFFFFF)  // 还原为100%不透明
                        .start();
                    
                    // 执行切换逻辑
                    preSwitch();
                })
                .start();
        });
        // 按钮最后添加，确保在最上层接收点击事件
        this.addChild(toggleButton);

        // 缩放按钮
        this.addEventListener(UIEvents.TICK, event -> {
            if (current != null) {
                float scale = current.getCurrentScale();

                toggleButton.scale(scale);

            }
        });
    }

    public ElementGroup addChild(String id, BaseRoot element) {
        children.put(id, element);
        element.setId(id);

        // 默认隐藏，只有第一个元素显示
        if (current == null) {
            current = element;
            element.setDisplay(true);
            // 第一次添加时初始化按钮贴图
            updateToggleButtonTexture(id);
        } else {
            element.setDisplay(false);
        }

        // 将元素添加到容器中（按钮下方）
        toggleElements.addChild(element);

        return this;
    }

    /**
     * 更新切换按钮的贴图
     */
    private void updateToggleButtonTexture(String id) {
        var texture = textures.get(id);
        var hoveredTexture = hoveredTextures.get(id);

        if (texture != null && hoveredTexture != null) {
            toggleButton.buttonStyle(style -> {
                style.baseTexture(texture);
                style.hoverTexture(hoveredTexture);
                style.pressedTexture(hoveredTexture);
            });
        }
    }

    public ElementGroup addChildTexture(String id, IGuiTexture texture) {
        textures.put(id, texture);
        return this;
    }

    public ElementGroup addHoveredTexture(String id, IGuiTexture hoverTexture) {
        hoveredTextures.put(id, hoverTexture);
        return this;
    }

    public float getCurrentScale() {
        if (current == null) {
            return 1;
        }else return current.getCurrentScale();
    }

    /**
     * 1.切换currentRoot
     * 2.切换按钮图标
     */
    private void preSwitch() {
        if (current == null) return;

        //保存上一个current
        lastCurrent = current;

        //隐藏当前元素
        current.setDisplay(false);

        //将current 切换到下一个
        var elementList = children.values().stream().toList();
        var currentIndex = elementList.indexOf(current);
        var nextIndex = (currentIndex + 1) % elementList.size();
        current = elementList.get(nextIndex);

        //显示新元素
        current.setDisplay(true);

        //切换按钮图标
        updateToggleButtonTexture(current.getId());

        onSwitch();
    }

    /**
     * TODO: 播放lastCurrent组件的退场动画
     * TODO: 播放currentRoot的入场动画
     */
    private void onSwitch() {
        finishSwitch();
    }

    /**
     * TODO: 后续处理，如状态重置、事件触发等
     */
    private void finishSwitch() {
        // TODO: 实现后续处理逻辑
    }

}
