package org.biotech.ui.gene_inventroy.element.inventory;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.Getter;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.biotech.ui.IScalable;

import java.util.ArrayList;
import java.util.List;


@Getter
public class GeneInventoryLine extends UIElement implements IScalable {

    private final List<GeneSlot> slots = new ArrayList<>();
    private UIElement slotsContainer; // 包含所有slot的可移动容器

    private float baseOffsetWidth;
    private int slotCountPerRow = 9;

    public static final int baseHeight = 21;

    public GeneInventoryLine(int lineNumber, float baseOffsetWidth, int startIndex, int slotCountPerRow, IItemHandlerModifiable handler) {
        super();
        this.slotCountPerRow = slotCountPerRow;
        this.baseOffsetWidth = baseOffsetWidth;

        this.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
            layout.paddingLeft(baseOffsetWidth); // 左侧偏移
        });

            slotsContainer = new UIElement();
            slotsContainer.layout(layout -> {
                layout.flexDirection(FlexDirection.ROW);
            });
            this.addChild(slotsContainer);

        // 创建槽位
        for (int i = 0; i < slotCountPerRow; i++) {
            int slotIndex = startIndex + i;
            GeneSlot.SlotPos pos = getSlotPos(i, slotCountPerRow);

            GeneSlot slot = new GeneSlot();
            slot.setSlotPos(pos);
            // id格式: 行数_行内index_总体index
            slot.setId("line" + lineNumber + "_idx" + i + "_slot" + slotIndex);
            slot.bind(handler, slotIndex);
            // Bind may reset part of slot metadata; apply quick-move style after bind.
            slot.getSlotStyle().acceptQuickMove(true);
            slot.getSlotStyle().isPlayerSlot(true);
            slot.getSlotStyle().quickMovePriority(100);

            slots.add(slot);
            slotsContainer.addChild(slot);
        }
    }

    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.paddingLeft(baseOffsetWidth * scale);
        });

        for (GeneSlot slot : slots) {
            slot.scale(scale);
        }

    }

    /**
     * 根据位置确定 SlotPos
     */
    private GeneSlot.SlotPos getSlotPos(int index, int total) {
        if (index == 0) {
            return GeneSlot.SlotPos.Left;
        } else if (index == total - 1) {
            return GeneSlot.SlotPos.Right;
        } else {
            return GeneSlot.SlotPos.Middle;
        }
    }

    
}
