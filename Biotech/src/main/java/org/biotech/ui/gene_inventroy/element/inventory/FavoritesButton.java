package org.biotech.ui.gene_inventroy.element.inventory;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;

/**
 * 收藏按钮
 * 支持自定义图片和三种状态（普通/悬停/按下）
 */
public class FavoritesButton extends Button implements IScalable {

    public final int baseWidth = 17;
    public final int baseHeight = 16;

    public static final IGuiTexture baseTexture = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(0, 207, 19, 18);
    public static final IGuiTexture hoverTexture = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(20, 207, 19, 18);
    public static final IGuiTexture pressedTexture = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(20, 207, 19, 18);

    public FavoritesButton() {
        super();
        // 隐藏文字，只显示图标
        this.noText();
        
        // 设置按钮样式（三种状态纹理）
        this.buttonStyle(style -> {
            style.baseTexture(baseTexture);
            style.hoverTexture(hoverTexture);
            style.pressedTexture(pressedTexture);
        });
        
        // 设置按钮尺寸
        this.layout(layout -> {
            layout.width(baseWidth);
            layout.height(baseHeight);
        });
    }


    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.width((int)(baseWidth * scale));
            layout.height((int)(baseHeight * scale));
        });
    }
}
