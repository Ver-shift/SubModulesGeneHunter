package org.biotech.ui.gene_inventroy.element.merge;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import lombok.Getter;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;

/**
 * 合并输入槽位组件
 * 待填写：贴图位置、尺寸、悬停偏移量
 */
public class MergeInputSlot extends ItemSlot implements IScalable {

    @Getter
    private SlotPos slotPos = SlotPos.Middle;

    // 待填写：基础 padding 值
    public static final int basePaddingTop = 1;
    public static final int basePaddingBottom = 2;

    public MergeInputSlot() {
        super();
        this.slotStyle(style -> {
            style.acceptQuickMove(true);
            style.isPlayerSlot(false);
            style.quickMovePriority(200);
        });
        applySlotStyle();
    }

    /**
     * 设置槽位位置并更新样式
     */
    public MergeInputSlot setSlotPos(SlotPos slotPos) {
        this.slotPos = slotPos;
        applySlotStyle();
        return this;
    }

    /**
     * 应用槽位样式和布局
     */
    private void applySlotStyle() {
        this.layout(layout -> {
            layout.width(slotPos.getBaseWidth());
            layout.height(slotPos.getBaseHeight() + basePaddingTop + basePaddingBottom);
            layout.paddingTop(basePaddingTop);
            layout.paddingBottom(basePaddingBottom);
            layout.paddingLeft(0);
            layout.paddingRight(0);
        });
    }

    @Override
    public void drawBackgroundTexture(GUIContext guiContext) {
        // 禁用默认背景绘制
    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        // 待填写：自定义背景/悬停绘制
    }

    /**
     * 检查是否悬停
     */
    public boolean isSlotHovered() {
        return isHover() || isSelfOrChildHover();
    }

    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.width(slotPos.getBaseWidth() * scale);
            layout.height((slotPos.getBaseHeight() + basePaddingTop + basePaddingBottom) * scale);
            layout.paddingTop(basePaddingTop * scale);
            layout.paddingBottom(basePaddingBottom * scale);
        });
    }

    /**
     * 槽位位置枚举
     * 待填写：纹理坐标、尺寸、悬停偏移量
     */
    @Getter
    public enum SlotPos {
        // 格式: 纹理X, 纹理Y, 宽度, 高度, 悬停偏移X, 悬停偏移Y
        // 示例：Left(0, 0, 18, 18, 0, 0),
        Left(1, 155, 20, 23, -1, -1),      // 待修改
        Middle(26, 155, 18, 23, -3, -1),    // 待修改
        Right(49, 155, 20, 23, -3, -1),     // 待修改
        HoverOverlay(0, 180, 24, 25, 0, 0); // 待修改

        final int x, y, baseWidth, baseHeight;
        final int hoverOffsetX, hoverOffsetY;

        SlotPos(int x, int y, int baseWidth, int baseHeight, int hoverOffsetX, int hoverOffsetY) {
            this.x = x;
            this.y = y;
            this.baseWidth = baseWidth;
            this.baseHeight = baseHeight;
            this.hoverOffsetX = hoverOffsetX;
            this.hoverOffsetY = hoverOffsetY;
        }

        public IGuiTexture getTexture() {
            // 待填写：正确的纹理路径
            return SpriteTexture.of(BiotechTexture.GUI_TEXTURE).setSprite(x, y, baseWidth, baseHeight);
        }
    }
}
