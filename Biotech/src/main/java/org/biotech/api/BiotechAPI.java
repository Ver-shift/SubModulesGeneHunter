package org.biotech.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.biotech.Biotech;
import org.biotech.api.system.choice.ChoiceManager;
import org.biotech.api.system.choice.core.IChoiceManager;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;
import org.biotech.api.init.AttachInit;
import org.biotech.api.init.CapInit;
import org.biotech.api.init.MenuInit;
import org.biotech.api.system.loot.core.ILootTableManager;
import org.biotech.api.system.merge.core.IMergeManager;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;


/**
 * Biotech API - 提供数据获取和界面打开等公共接口
 */
public class BiotechAPI {

    /**
     * 获取玩家的基因数据
     * 客户端和服务端都能调用
     */
    public static GeneData getGeneData(Player player) {
        return player.getData(AttachInit.GENE_DATA);
    }

    public static IGeneInventoryManager getGeneInventoryManager(Player player) {
        return player.getCapability(CapInit.GENE_INVENTORY);
    }

    public static ILootTableManager getLootTableManager(Player player) {
        return player.getCapability(CapInit.LOOT_TABLE);
    }

    public static IMergeManager getMergeManager(Player player) {
        return player.getCapability(CapInit.MERGE_MANAGER);
    }

    /**
     * 为目标玩家打开基因库存界面（用于服务端操作其他玩家）
     * @param targetPlayer 目标玩家
     */
    public static void openGeneInventoryFor(ServerPlayer targetPlayer) {

        var manager = getGeneInventoryManager(targetPlayer);
        boolean isOpened = manager != null && manager.openGeneMenu();
        if (isOpened) {
            Biotech.LOGGER.info("Opened gene inventory for {}", targetPlayer.getGameProfile().getName());
        } else {
            Biotech.LOGGER.error("Failed to open gene inventory for {}", targetPlayer.getGameProfile().getName());
        }
    }

    public static IChoiceManager getChoiceManager(Player player) {
        return new ChoiceManager(player);
    }

    public static void openGeneChoiceFor(ServerPlayer targetPlayer) {
        var manager = getChoiceManager(targetPlayer);
        boolean isOpened = manager != null && manager.openChoiceMenu();
        if (isOpened) {
            Biotech.LOGGER.info("Opened gene choice for {}", targetPlayer.getGameProfile().getName());
        } else {
            Biotech.LOGGER.error("Failed to open gene choice for {}", targetPlayer.getGameProfile().getName());
        }
    }

    public static final String GENE_EQUIP_SLOT = "gene_equip_slot";
    public static final String XENE_EQUIP_SLOT = "xene_equip_slot";

    public static IDynamicStackHandler getGeneEquipSlots(Player targetPlayer) {
        return getCuriosSlots(targetPlayer, GENE_EQUIP_SLOT);
    }

    public static IDynamicStackHandler getXeneEquipSlots(Player targetPlayer) {
        return getCuriosSlots(targetPlayer, XENE_EQUIP_SLOT);
    }

    private static IDynamicStackHandler getCuriosSlots(Player targetPlayer, String slotId) {
        return CuriosApi.getCuriosInventory(targetPlayer)
                .map(curiosItemHandler -> {
                    var handle = curiosItemHandler.getCurios().get(slotId);
                    return handle != null ? handle.getStacks() : null;
                })
                .orElse(null);
    }
}
