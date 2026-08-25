package org.galaxy.gene_hunter.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent.WeaponShape;

@EventBusSubscriber(modid = GeneHunter.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class GeneHunterClientEvents {

    private GeneHunterClientEvents() {
    }

    @SubscribeEvent
    public static void addWeaponClassTooltip(ItemTooltipEvent event) {
        WeaponShape shape = shapeFromTag(event.getItemStack());
        if (shape == null) {
            var weaponClass = event.getItemStack().get(GeneHunterComponentInit.WEAPON_CLASS.get());
            shape = weaponClass == null ? null : weaponClass.shape();
        }
        if (shape == null) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.gene_hunter.weapon_class." + shape.id())
                .withStyle(ChatFormatting.GRAY));
    }

    /** Tags are authoritative for both datapack additions and Simply Swords' optional weapons. */
    private static WeaponShape shapeFromTag(net.minecraft.world.item.ItemStack stack) {
        if (stack.is(GeneHunterTags.BLADE_WEAPON)) return WeaponShape.BLADE;
        if (stack.is(GeneHunterTags.SWORD_WEAPON)) return WeaponShape.SWORD;
        if (stack.is(GeneHunterTags.AXE_WEAPON)) return WeaponShape.AXE;
        if (stack.is(GeneHunterTags.HAMMER_WEAPON)) return WeaponShape.HAMMER;
        return null;
    }
}
