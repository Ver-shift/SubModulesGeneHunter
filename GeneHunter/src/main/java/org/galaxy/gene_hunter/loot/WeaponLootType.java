package org.galaxy.gene_hunter.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.util.ItemType;
import org.galaxylib.api.system.loot.core.ILootType;

/**
 * 武器战利品类型 - 只处理有攻击伤害属性的物品
 */
public class WeaponLootType implements ILootType<ItemStack> {
    
    @Override
    public String getName() {
        return "weapon";
    }

    @Override
    public int getPoolCount(ServerPlayer player) {
        return GeneHunterAPI.getChoiceManager(player).getChoiceCount();
    }

    @Override
    public ItemStack getLoot(ResourceLocation lootId, int count) {
        // 从注册表获取物品
        var item = BuiltInRegistries.ITEM.get(lootId);
        
        // 检查物品是否存在
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        
        // 创建物品栈
        ItemStack stack = new ItemStack(item, count);
        
        // 检测是否是武器（有攻击伤害属性）
        if (ItemType.isWeapon(stack)) {
            return stack;
        }
        
        return ItemStack.EMPTY;
    }




}
