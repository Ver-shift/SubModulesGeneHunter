package org.biotech.ui.gene_inventroy.element.merge;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import org.biotech.item.GeneItem;
import org.biotech.ui.IScalable;

import java.util.ArrayList;
import java.util.List;

import static org.biotech.ui.gene_inventroy.element.merge.MergeInputSlot.SlotPos;

/**
 * 合并输入槽位行组件
 * 包含多个 MergeInputSlot，支持阶梯偏移布局
 */
@Getter
public class MergeInputSlotLine extends UIElement implements IScalable {
    private static final float ROMAN_TEXT_SCALE = 0.62f;

    private final List<MergeInputSlot> slots = new ArrayList<>();
    private UIElement slotsContainer; // 包含所有slot的可移动容器

    private float baseOffsetWidth;
    private int slotCountPerRow = 9; // 待修改：每行槽位数量
    private float currentScale;

    // 待填写：基础高度
    public static final int baseHeight = 21;

    /**
     * 构造函数
     * @param lineNumber 行号
     * @param baseOffsetWidth 基础偏移宽度（用于阶梯布局）
     * @param startIndex 起始槽位索引
     * @param slotCountPerRow 每行槽位数量
     * @param handler 物品处理器
     */
    public MergeInputSlotLine(int lineNumber, float baseOffsetWidth, int startIndex, int slotCountPerRow, IItemHandlerModifiable handler) {
        super();
        int availableSlots = handler == null ? slotCountPerRow : Math.max(0, handler.getSlots() - startIndex);
        int actualSlotCount = Math.min(slotCountPerRow, availableSlots);
        this.slotCountPerRow = actualSlotCount;
        this.baseOffsetWidth = baseOffsetWidth;

        this.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
            layout.paddingLeft(baseOffsetWidth); // 左侧偏移（阶梯布局）
        });

        slotsContainer = new UIElement();
        slotsContainer.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
        });
        slotsContainer.setVisible(true);
        this.addChild(slotsContainer);

        // 创建槽位
        for (int i = 0; i < actualSlotCount; i++) {
            int slotIndex = startIndex + i;
            SlotPos pos = getSlotPos(i, actualSlotCount);

            MergeInputSlot slot = new MergeInputSlot();
            slot.setSlotPos(pos);
            // id格式: 行数_行内index_总体index
            slot.setId("merge_input_line" + lineNumber + "_idx" + i + "_slot" + slotIndex);
            slot.slotStyle(style -> {
                style.acceptQuickMove(true);
                style.isPlayerSlot(false);
                style.quickMovePriority(200);
            });
            if (handler != null) {
                slot.bind(handler, slotIndex);
            }

            slots.add(slot);
            slotsContainer.addChild(slot);
        }
    }

    @Override
    public void scale(float scale) {
        this.currentScale = scale;
        this.layout(layout -> {
            layout.paddingLeft(baseOffsetWidth * scale);
        });

        for (MergeInputSlot slot : slots) {
            slot.scale(scale);
        }
    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        // 通过 slotElements 找到当前显示的页面，然后绘制其中的槽位
        if (!slotsContainer.isVisible()) return;

        // 调用 PageSlotContainer 的绘制方法
        drawSlots(guiContext,currentScale);

    }

    /**
     * 绘制行内的槽位（背景和悬停效果）
     */
    public void drawSlots(GUIContext guiContext, float scale) {
        // 先绘制所有非悬停槽位的背景
        for (MergeInputSlot slot : slots) {
            if (!slot.isSlotHovered()) {
                drawSlotOverlay(guiContext, slot, scale);
            }
        }

        // 再绘制悬停槽位的效果（确保在最上层）
        for (MergeInputSlot slot : slots) {
            if (slot.isSlotHovered()) {
                drawSlotHover(guiContext, slot, scale);
            }
            drawItemStack(guiContext, slot, scale);
        }
    }

    /**
     * 绘制槽位背景纹理
     */
    private void drawSlotOverlay(GUIContext context, MergeInputSlot slot, float scale) {
        var slotPos = slot.getSlotPos();
        var texture = slotPos.getTexture();
        int width = slotPos.getBaseWidth();
        int height = slotPos.getBaseHeight();

        // 使用 content 区域坐标（相对于屏幕的绝对坐标，已排除 padding）
        float x = slot.getContentX();
        float y = slot.getContentY();

        context.pose.pushPose();
        context.pose.translate(x, y, 0);
        context.pose.scale(scale, scale, 1);
        context.drawTexture(texture, 0, 0, width, height);
        context.pose.popPose();
    }

    /**
     * 绘制悬停纹理
     */
    private void drawSlotHover(GUIContext context, MergeInputSlot slot, float scale) {
        var hoverPos = MergeInputSlot.SlotPos.HoverOverlay;
        var slotPos = slot.getSlotPos();
        var texture = hoverPos.getTexture();
        int width = hoverPos.getBaseWidth();
        int height = hoverPos.getBaseHeight();

        // 使用 content 区域坐标（相对于屏幕的绝对坐标，已排除 padding）
        float x = slot.getContentX();
        float y = slot.getContentY();

        context.pose.pushPose();
        context.pose.translate(x, y, 1);
        context.pose.scale(scale, scale, 1);
        context.drawTexture(texture, slotPos.getHoverOffsetX(), slotPos.getHoverOffsetY(), width, height);
        context.pose.popPose();
    }

    private void drawItemStack(GUIContext context, MergeInputSlot slot, float scale) {
        ItemStack itemStack = slot.getSlot().getItem();
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof GeneItem)) {
            return;
        }

        ResourceLocation textureResource = IStackTraitAccess.getSelectedTraitTexture(itemStack);
        if (textureResource == null) {
            return;
        }
        IGuiTexture texture = SpriteTexture.of(textureResource);

        var slotPos = slot.getSlotPos();
        float x = slot.getContentX();
        float y = slot.getContentY();

        context.pose.pushPose();
        context.pose.translate(x, y, 0);
        context.pose.scale(scale, scale, 1);
        context.drawTexture(texture, slotPos.getHoverOffsetX() + 6, slotPos.getHoverOffsetY() + 6, 12, 12);

        int traitCount = IStackTraitAccess.getTraitCount(itemStack);
        if (traitCount > 1) {
            Font font = Minecraft.getInstance().font;
            String roman = toRoman(traitCount);
            int badgeX = 16 - Math.round(font.width(roman) * ROMAN_TEXT_SCALE);
            int badgeY = 11;

            context.pose.pushPose();
            context.pose.translate(badgeX, badgeY, 200);
            context.pose.scale(ROMAN_TEXT_SCALE, ROMAN_TEXT_SCALE, 1.0F);
            context.graphics.drawString(font, roman, slotPos.getHoverOffsetX()+2, slotPos.getHoverOffsetY()+2, 0xFFFFE6FF, true);
            context.pose.popPose();
        }

        context.pose.popPose();
    }


    private static String toRoman(int value) {
        int[] numbers = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder result = new StringBuilder();
        int remaining = Math.max(value, 0);

        for (int i = 0; i < numbers.length && remaining > 0; i++) {
            while (remaining >= numbers[i]) {
                result.append(symbols[i]);
                remaining -= numbers[i];
            }
        }
        return result.toString();
    }

    /**
     * 根据位置确定 SlotPos
     */
    private SlotPos getSlotPos(int index, int total) {
        if (index == 0) {
            return SlotPos.Left;
        } else if (index == total - 1) {
            return SlotPos.Right;
        } else {
            return SlotPos.Middle;
        }
    }
}
