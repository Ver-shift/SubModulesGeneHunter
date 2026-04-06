package org.biotech.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.BiotechDataComponentInit;
import org.biotech.api.init.BiotechItemInit;
import org.biotech.api.init.BiotechTraitInit;
import org.galaxylib.api.system.loot.core.ILootTableManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * 效果词条类型
 */
public class XeneTraitLootType implements ILootType<ITrait> {

    @Override
    public String getName() {
        return "xene_trait";
    }

    @Override
    public ITrait getLoot(ResourceLocation lootId, int count) {
        return BiotechTraitInit.getTraitById(lootId);
    }

    /**
     * 创建一个基因槽位，同时再将物品放进去
     * @param player 玩家
     * @param lootResult 抽取结果
     */
    @Override
    public void claimResultsToPlayer(ServerPlayer player, ILootTableManager.LootResult lootResult) {
        // 创建 TraitComp 并收集所有词条
        TraitComp comp = TraitComp.empty();
        for (var entry : lootResult.result()) {
            ITrait trait = getLoot(entry.getId(), entry.getCount());
            if (trait != null) {
                comp.addTrait(trait);
            }
        }

        // 如果收集到了词条，创建带 TraitComp 的 XeneItem 并放入 xene_equip_slot 槽位
        if (!comp.isEmpty()) {
            ItemStack xeneStack = new ItemStack(BiotechItemInit.XENE_ITEM.get());
            xeneStack.set(BiotechDataComponentInit.TRAIT_COMP.get(), comp);

            IDynamicStackHandler slotHandler = BiotechAPI.getXeneEquipSlots(player);
            if (slotHandler == null) {
                return;
            }

            // 找到第一个空槽位并放入物品
            for (int i = 0; i < slotHandler.getSlots(); i++) {
                if (slotHandler.getStackInSlot(i).isEmpty()) {
                    slotHandler.setStackInSlot(i, xeneStack);
                    break;
                }
            }
        }
    }


}
