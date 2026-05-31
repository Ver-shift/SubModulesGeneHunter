package org.galaxy.gene_hunter.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.util.ItemType;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;

import java.util.Optional;

/**
 * 武器战利品类型 - 只处理有攻击伤害属性的物品
 */
public class WeaponLootType implements ILootType<ItemStack> {
    
    @Override
    public int getPoolCount(ServerPlayer player) {
        return GeneHunterAPI.getChoiceManager(player).getChoiceCount();
    }

    @Override
    public Optional<ItemStack> resolve(LootEntryDefinition entry, LootManager.Context context) {
        var item = BuiltInRegistries.ITEM.get(entry.id());
        
        // 检查物品是否存在
        if (item == Items.AIR) {
            return Optional.empty();
        }
        
        // 创建物品栈
        ItemStack stack = new ItemStack(item, entry.count());
        
        // 检测是否是武器（有攻击伤害属性）
        if (ItemType.isWeapon(stack)) {
            return Optional.of(stack);
        }
        
        return Optional.empty();
    }




}
