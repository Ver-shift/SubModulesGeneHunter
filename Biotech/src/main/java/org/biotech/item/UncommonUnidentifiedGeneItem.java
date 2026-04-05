package org.biotech.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.biotech.api.system.gene.core.IUnidentifiedGeneItem;

/**
 * 少见未解析核心 - 极小概率出双词条
 */
public class UncommonUnidentifiedGeneItem extends Item implements IUnidentifiedGeneItem {

    public static final Rarity RARITY = Rarity.UNCOMMON;

    public UncommonUnidentifiedGeneItem() {
        super(new Properties()
                .stacksTo(64)
                .rarity(RARITY)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return use(level, player, usedHand, RARITY);
    }
}
