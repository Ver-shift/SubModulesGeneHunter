package org.galaxy.beyond.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.NotNull;

public class InfiniteIronSwordItem extends SwordItem {

    public InfiniteIronSwordItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(createAttributes(Tiers.IRON, 3F, -2.4F)));
    }

    @Override
    public void postHurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
    }
}
