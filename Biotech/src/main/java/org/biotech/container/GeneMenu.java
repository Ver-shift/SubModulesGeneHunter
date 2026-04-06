package org.biotech.container;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.BiotechMenuInit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GeneMenu extends ModularUIContainerMenu {

    private enum SourceBranch {
        INVENTORY,
        MERGE,
        UNKNOWN
    }

    private SourceBranch quickMoveSourceBranch = SourceBranch.UNKNOWN;

    public GeneMenu(int windowID, Inventory inventory, IContainerUIHolder uiHolder) {
        super((MenuType<ModularUIContainerMenu>) (MenuType<?>) BiotechMenuInit.GENE_MENU.get(), windowID, inventory, uiHolder);
    }

    @Override
    public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int idx) {
        if (player.level().isClientSide) {
            return ItemStack.EMPTY;
        }
        if (idx < 0 || idx >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        var clickSlot = this.slots.get(idx);
        if (!clickSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        var stackToMove = clickSlot.getItem();
        if (stackToMove.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Source must be biotech gene/favorite inventory slots.
        var sourceItemSlot = asModularUIHolderMenu().getItemSlot(clickSlot);
        if (!isGeneInventorySource(sourceItemSlot)) {
            return ItemStack.EMPTY;
        }

        var original = stackToMove.copy();
        var sourceBranch = resolveSourceBranch(sourceItemSlot);
        this.quickMoveSourceBranch = sourceBranch;

        ItemStack remaining;
        try {
            remaining = performQuickMoveStack(stackToMove, true);
        } finally {
            this.quickMoveSourceBranch = SourceBranch.UNKNOWN;
        }

        if (!ItemStack.matches(original, remaining)) {
            clickSlot.setByPlayer(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            return original;
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean isPlayerSideSlot(Slot slot) {
        if (slot.container == inventory) {
            return true;
        }
        var itemSlot = asModularUIHolderMenu().getItemSlot(slot);
        if (itemSlot == null) {
            return super.isPlayerSideSlot(slot);
        }
        var id = itemSlot.getId();
        if (id.isEmpty()) {
            return super.isPlayerSideSlot(slot);
        }
        if (isGeneInventorySourceId(id)) {
            return true;
        }
        if (isMergeInputSlotId(id) || isEquipSlotId(id) || id.contains("output")) {
            return false;
        }
        return super.isPlayerSideSlot(slot);
    }

    @Override
    protected boolean isValidQuickMoveDestination(Slot candidateSlot, ItemStack stackToMove, boolean fromPlayerSide) {
        ItemSlot itemSlot = asModularUIHolderMenu().getItemSlot(candidateSlot);
        if (itemSlot == null) {
            return false;
        }
        String id = itemSlot.getId();
        if (id.isEmpty() || !itemSlot.getSlotStyle().acceptQuickMove()) {
            return false;
        }
        if (!candidateSlot.mayPlace(stackToMove)) {
            return false;
        }

        boolean isEquipSlot = isEquipSlotId(id);
        boolean isMergeInputSlot = isMergeInputSlotId(id);

        // Only allow gene/favorite slots (source) -> equip or merge input (destination).
        if (!fromPlayerSide) {
            return false;
        }
        if (isEquipSlot && BiotechAPI.getGeneEquipSlots(inventory.player) == null) {
            return false;
        }

        return switch (quickMoveSourceBranch) {
            case INVENTORY -> isEquipSlot;
            case MERGE -> isMergeInputSlot;
            case UNKNOWN -> isEquipSlot || isMergeInputSlot;
        };
    }

    @Override
    protected int getQuickMovePriority(Slot slot) {
        ItemSlot itemSlot = asModularUIHolderMenu().getItemSlot(slot);
        if (itemSlot == null) {
            return 0;
        }

        String id = itemSlot.getId();
        if (id.isEmpty()) {
            return 0;
        }
        if (isMergeInputSlotId(id)) {
            return 220;
        }
        if (isEquipSlotId(id)) {
            return 200;
        }
        if (isGeneInventorySourceId(id)) {
            return 100;
        }
        if (id.contains("output")) {
            return -100;
        }
        return 0;
    }

    private static boolean isGeneInventorySource(ItemSlot slot) {
        if (slot == null) {
            return false;
        }
        String id = slot.getId();
        return isGeneInventorySourceId(id);
    }

    private static boolean isGeneInventorySourceId(String id) {
        return id.startsWith("line") && !isMergeInputSlotId(id) && !isEquipSlotId(id) && !id.contains("output");
    }

    private static boolean isMergeInputSlotId(String id) {
        return id.startsWith("merge_input_line");
    }

    private static boolean isEquipSlotId(String id) {
        return id.startsWith("equip_slot_");
    }

    private static SourceBranch resolveSourceBranch(ItemSlot itemSlot) {
        if (itemSlot == null) {
            return SourceBranch.UNKNOWN;
        }

        var parent = itemSlot.getParent();
        while (parent != null) {
            var id = parent.getId();
            if ("inventory".equals(id)) {
                return SourceBranch.INVENTORY;
            }
            if ("merge".equals(id) || "merges".equals(id) || id.startsWith("merge")) {
                return SourceBranch.MERGE;
            }
            parent = parent.getParent();
        }
        return SourceBranch.UNKNOWN;
    }
}
