package org.galaxy.beyond.item;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import org.galaxy.beyond.api.client.gui.UIProvider;

public class TextItem extends Item implements HeldItemUIMenuType.HeldItemUI {

    public TextItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
//            HeldItemUIMenuType.openUI(serverPlayer, hand);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
        }
//        PieceBeardifierModifier
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        return UIProvider.createSceneUI(holder.player);
    }

    @Override
    public Component getUIDisplayName(HeldItemUIMenuType.HeldItemUIHolder holder) {
        return Component.literal("Scene UI");
    }
}
