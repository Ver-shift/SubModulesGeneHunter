package org.biotech.ui.gene_inventroy.element.inventory;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;

/**
 * 翻页按钮组件 - 用于切换页面
 * 
 * 大小：18 x 14 像素
 * 
 * 使用方式：
 * 1. 创建并设置位置
 * 2. 设置点击回调（可选）
 * 3. 使用 setTexture() 设置自定义纹理
 */
@Getter
@Accessors(chain = true)
public class SwitchPage extends Button implements IScalable {

    public static final int baseWidth = 18;
    public static final int baseHeight = 14;

    // 纹理常量
    private static final IGuiTexture LEFT_TEXTURE = SpriteTexture.of(BiotechTexture.GUI_TEXTURE).setSprite(0, 226, 18, 14);
    private static final IGuiTexture HOVER_LEFT_TEXTURE = SpriteTexture.of(BiotechTexture.GUI_TEXTURE).setSprite(19, 226, 18, 14);
    private static final IGuiTexture RIGHT_TEXTURE = SpriteTexture.of(BiotechTexture.GUI_TEXTURE).setSprite(0, 241, 18, 14);
    private static final IGuiTexture HOVER_RIGHT_TEXTURE = SpriteTexture.of(BiotechTexture.GUI_TEXTURE).setSprite(19, 241, 18, 14);


    private final boolean nextButton;


    public SwitchPage(boolean isNextButton) {
        super();
        this.nextButton = isNextButton;
        
        // 隐藏文字
        this.noText();
        
        // 布局设置
        this.layout(layout -> {
            layout.width(baseWidth);
            layout.height(baseHeight);
        });
        // 设置按钮纹理（通过 getButtonStyle() 直接设置）
        IGuiTexture base = isNextButton ? RIGHT_TEXTURE : LEFT_TEXTURE;
        IGuiTexture hover = isNextButton ? HOVER_RIGHT_TEXTURE : HOVER_LEFT_TEXTURE;
        this.buttonStyle(style -> {
            style.baseTexture(base);
            style.hoverTexture(hover);
            style.pressedTexture(base);
        });

    }




    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.width((baseWidth * scale));
            layout.height((baseHeight * scale));
        });
    }
}
