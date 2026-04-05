package org.biotech.ui.gene_inventroy.element.inventory;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;

/**
 * 页码显示组件 - 显示 "1/3" 格式的页码
 * 
 * 支持自动拉伸背景纹理和文字居中
 */
@Getter
@Accessors(chain = true)
public class PageText extends Label implements IScalable {

    // 背景纹理（使用 9-slice 自动拉伸）
    private static final IGuiTexture BACKGROUND_TEXTURE = BiotechTexture.Button_Slice;

    // 翻译键
    private static final String TRANSLATION_KEY_GENE = "biotech.ui.gene_inventory.gene_mode";
    private static final String TRANSLATION_KEY_FAVORITE = "biotech.ui.gene_inventory.favorite_mode";

    @Setter private int currentPage = 0;
    @Setter private int totalPages = 1;
    private boolean isFavoritesMode = false;

    public static final int baseWidth = 48;
    public static final int baseHeight = 14;
    public static final int baseFontSize = 9;
    
    // 浅绿色文本颜色 #94af60
    private static final int TEXT_COLOR = 0x94af60;

    public PageText() {
        super();
        
        // 布局设置 - 使用百分比宽度支持自动拉伸
        this.layout(layout -> {
            layout.width(baseWidth);
            layout.height(baseHeight);         // 高度固定
        });
        
        // 设置背景纹理
        this.style(style -> style.backgroundTexture(BACKGROUND_TEXTURE));
        
        // 文字居中
        this.textStyle(style -> style
                .textColor(TEXT_COLOR)
                .textAlignHorizontal(Horizontal.CENTER)
                .textAlignVertical(Vertical.CENTER));

        updateText();
    }

    /**
     * 更新页码显示
     */
    public void updateText() {
        String modeKey = isFavoritesMode ? TRANSLATION_KEY_FAVORITE : TRANSLATION_KEY_GENE;
        Component text = Component.empty()
                .append(Component.translatable(modeKey))
                .append(" ")
                .append(Component.literal((currentPage + 1) + "/" + totalPages));
        this.setText(text);
    }

    /**
     * 设置页码并更新显示
     */
    public PageText setPage(int current, int total) {
        this.currentPage = current;
        this.totalPages = total;
        updateText();
        return this;
    }

    /**
     * 设置模式并更新显示
     * @param favoritesMode true=收藏模式, false=普通模式
     */
    public PageText setFavoritesMode(boolean favoritesMode) {
        this.isFavoritesMode = favoritesMode;
        updateText();
        return this;
    }

    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.width((baseWidth * scale));
            layout.height((baseHeight * scale));
        });
        this.textStyle(style -> style.fontSize(baseFontSize * scale));
    }
}