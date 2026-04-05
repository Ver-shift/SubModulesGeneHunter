package org.biotech.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.DataComponentInit;
import org.biotech.component.Example;

/**
 * 基因文本物品 - 右键打开基因界面
 */
public class TextGeneItem extends Item {

    public TextGeneItem() {
        super(new Properties()
                .component(DataComponents.RARITY, Rarity.UNCOMMON)
                .component(DataComponentInit.EXAMPLE.get(), new Example()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            BiotechAPI.openGeneInventoryFor(serverPlayer);
        }


        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
