package org.galaxy.gene_hunter.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;

@EventBusSubscriber(modid = GeneHunter.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class GeneHunterClientEvents {

    private GeneHunterClientEvents() {
    }

    @SubscribeEvent
    public static void addWeaponClassTooltip(ItemTooltipEvent event) {
        var weaponClass = event.getItemStack().get(GeneHunterComponentInit.WEAPON_CLASS.get());
        if (weaponClass == null) {
            return;
        }
        event.getToolTip().add(Component.translatable(weaponClass.translationKey()).withStyle(ChatFormatting.GRAY));
    }
}
