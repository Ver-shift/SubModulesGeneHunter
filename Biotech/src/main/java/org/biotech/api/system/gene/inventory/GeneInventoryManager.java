package org.biotech.api.system.gene.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.biotech.api.init.BiotechGeneInit;
import org.biotech.api.init.BiotechMenuInit;
import org.biotech.api.system.GeneData;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;
import org.biotech.container.GeneMenu;
import org.biotech.item.GeneItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 基因背包管理器 - 参考 Inventory 类的设计
 */
public class GeneInventoryManager implements IGeneInventoryManager {

    private final GeneData data;
    private final PlayerGeneInventoryData inventoryData;

    public GeneInventoryManager(GeneData data) {
        this.data = data;
        this.inventoryData = this.data.getPlayerGeneInventoryData();
    }

    @Override
    public boolean openGeneMenu() {
        var player = data.getPlayer();
        if (player == null) {
            return false;
        }
        var opened = player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, p) -> new GeneMenu(containerId, inventory, BiotechMenuInit.GENE_MENU_CONTAINER),
                Component.empty())
        );
        return opened.isPresent();
    }



    // ========================= Inventory 槽位控制 =========================

    @Override
    public AddResult add(GeneItem item) {
        return add(-1, item);
    }

    @Override
    public AddResult add(int slotIndex, GeneItem item) {
        if (item == null) {
            return AddResult.EMPTY_ITEM;
        }
        return add(slotIndex, item.getDefaultInstance());
    }

    @Override
    public AddResult add(int slotIndex, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return AddResult.EMPTY_ITEM;
        }

        try {
            if (slotIndex == -1) {
                slotIndex = getFirstEmptySlot();
            }

            if (slotIndex >= 0 && slotIndex < PlayerGeneInventoryData.GENE_SLOT_COUNT) {
                inventoryData.getGeneSlots().setStackInSlot(slotIndex, stack);
                return AddResult.SUCCESS;
            } else if (slotIndex == -1) {
                return AddResult.FULL;
            } else {
                return AddResult.NO_SPACE;
            }
        } catch (Exception e) {
            return AddResult.INVALID_TYPE;
        }
    }

    @Override
    public AddResult add(ItemStack stack) {
        return add(-1, stack);
    }

    @Override
    public AddResult addUnidentified(Rarity rarity) {
        int slotIndex = getFirstEmptySlot();
        if (slotIndex == -1) {
            return AddResult.FULL;
        }

        var player = data.getPlayer();
        if (player == null) {
            return AddResult.INVALID_TYPE;
        }

        List<net.minecraft.resources.ResourceLocation> allGeneIds =
                BiotechGeneInit.getGeneIds(player.level().registryAccess());
        if (allGeneIds.isEmpty()) {
            return AddResult.INVALID_TYPE;
        }

        List<net.minecraft.resources.ResourceLocation> rarityMatches = new ArrayList<>();
        for (var id : allGeneIds) {
            BiotechGeneInit.getGene(player.level().registryAccess(), id)
                    .filter(definition -> definition.rarity() == rarity)
                    .ifPresent(definition -> rarityMatches.add(id));
        }

        // A datapack may intentionally define only some rarity tiers.  Falling back
        // keeps a core usable instead of silently consuming it with no result.
        List<net.minecraft.resources.ResourceLocation> candidates =
                rarityMatches.isEmpty() ? allGeneIds : rarityMatches;
        var geneId = candidates.get(player.getRandom().nextInt(candidates.size()));
        var definition = BiotechGeneInit.getGene(player.level().registryAccess(), geneId);
        return definition.map(value -> add(slotIndex, GeneItem.createForGene(geneId, value)))
                .orElse(AddResult.INVALID_TYPE);
    }

    @Override
    public int getFirstEmptySlot() {
        for (int i = 0; i < PlayerGeneInventoryData.GENE_SLOT_COUNT; i++) {
            if (inventoryData.getGeneSlots().getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean removeItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < PlayerGeneInventoryData.GENE_SLOT_COUNT) {
            inventoryData.getGeneSlots().setStackInSlot(slotIndex, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getItemStack(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < PlayerGeneInventoryData.GENE_SLOT_COUNT) {
            return inventoryData.getGeneSlots().getStackInSlot(slotIndex);
        }
        return ItemStack.EMPTY;
    }

    // ========================= Favorite 收藏槽位控制 =========================

    @Override
    public AddResult addToFavorite(GeneItem item) {
        return addToFavorite(-1, item);
    }

    @Override
    public AddResult addToFavorite(int slotIndex, GeneItem item) {
        if (item == null) {
            return AddResult.EMPTY_ITEM;
        }
        
        ItemStack stack = item.getDefaultInstance();
        
        try {
            if (slotIndex == -1) {
                slotIndex = getFirstEmptyFavoriteSlot();
            }
            
            if (slotIndex >= 0 && slotIndex < PlayerGeneInventoryData.FAVORITE_SLOT_COUNT) {
                inventoryData.getFavoriteSlots().setStackInSlot(slotIndex, stack);
                return AddResult.SUCCESS;
            } else if (slotIndex == -1) {
                return AddResult.FULL;
            } else {
                return AddResult.NO_SPACE;
            }
        } catch (Exception e) {
            return AddResult.INVALID_TYPE;
        }
    }

    @Override
    public int getFirstEmptyFavoriteSlot() {
        for (int i = 0; i < PlayerGeneInventoryData.FAVORITE_SLOT_COUNT; i++) {
            if (inventoryData.getFavoriteSlots().getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean removeFavoriteItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < PlayerGeneInventoryData.FAVORITE_SLOT_COUNT) {
            inventoryData.getFavoriteSlots().setStackInSlot(slotIndex, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getFavoriteItemStack(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < PlayerGeneInventoryData.FAVORITE_SLOT_COUNT) {
            return inventoryData.getFavoriteSlots().getStackInSlot(slotIndex);
        }
        return ItemStack.EMPTY;
    }
}
