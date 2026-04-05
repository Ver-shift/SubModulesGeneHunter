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
 * 史诗未解析核心 - 必定出双词条
 */
public class EpicUnidentifiedGeneItem extends Item implements IUnidentifiedGeneItem {

    public static final Rarity RARITY = Rarity.EPIC;

    public EpicUnidentifiedGeneItem() {
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
