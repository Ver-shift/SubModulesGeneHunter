package org.biotech.ui.gene_inventroy.element.inventory;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import lombok.Getter;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;

/**
 * 基因槽位组件，支持三种位置样式：Left, Middle, Right
 * 悬停时显示自定义悬停纹理而非原版背景
 */
public class GeneSlot extends ItemSlot implements IScalable {

    @Getter
    private SlotPos slotPos = SlotPos.Middle;
    private boolean wasHovered = false;
    public static final int basePaddingTop = 1;
    public static final int basePaddingBotton = 2;

    public GeneSlot() {
        super();
        this.slotStyle(style -> {
            style.acceptQuickMove(true);
            style.isPlayerSlot(true);
            style.quickMovePriority(10);
        });
        applySlotStyle();
    }

    /**
     * 设置槽位位置并更新样式
     */
    public GeneSlot setSlotPos(SlotPos slotPos) {
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
            layout.height(slotPos.getBaseHeight() + basePaddingTop + basePaddingBotton);
            layout.paddingTop(basePaddingTop);
            layout.paddingBottom(basePaddingBotton);
            layout.paddingLeft(0);
            layout.paddingRight(0);
        });
    }

    @Override
    public void drawBackgroundTexture(GUIContext guiContext) {

    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
    }

    /**
     * 检查是否悬停
     */
    public boolean isSlotHovered() {
        return isHover() || isSelfOrChildHover();
    }

    /**
     * 获取悬停贴图的 X 坐标
     */
    public float getHoverX() {
        return getPositionX() + slotPos.hoverOffsetX;
    }

    /**
     * 获取悬停贴图的 Y 坐标
     */
    public float getHoverY() {
        return getPositionY() + slotPos.hoverOffsetY;
    }


    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.width(slotPos.getBaseWidth() * scale);
            layout.height((slotPos.getBaseHeight() + basePaddingTop + basePaddingBotton) * scale);
            layout.paddingTop(basePaddingTop * scale);
            layout.paddingBottom(basePaddingBotton * scale);
        });
    }

    /**
     * 槽位在整体上面的位置
     */
    @Getter
    public enum SlotPos {
        // 槽位纹理位置 + 悬停偏移量 (hoverOffsetX, hoverOffsetY)
        // Left: 宽19，悬停宽22，中心靠右，偏移-1
        // Middle: 宽18，悬停宽22，居中，偏移-2
        // Right: 宽19，悬停宽22，中心靠左，偏移-2
        Left(1, 107, 19, 21, -1, -1),
        Middle(25, 107, 18, 21, -2, -1),
        Right(48, 107, 19, 21, -2, -1),
        HoverOverlay(0, 130, 22, 23, 0, 0);

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
            return SpriteTexture.of(BiotechTexture.GUI_TEXTURE).setSprite(x, y, baseWidth, baseHeight);
        }
    }
}
