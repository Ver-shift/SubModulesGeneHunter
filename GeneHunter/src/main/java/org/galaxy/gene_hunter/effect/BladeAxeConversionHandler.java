package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

/** Converts attack speed, sacrificed critical chance, and flat damage for blade and axe Xenes. */
@EventBusSubscriber(modid = GeneHunter.MODID)
public final class BladeAxeConversionHandler {
    private BladeAxeConversionHandler() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        boolean blade = player.getMainHandItem().is(GeneHunterTags.BLADE_WEAPON);
        boolean axe = player.getMainHandItem().is(GeneHunterTags.AXE_WEAPON);
        if (!blade && !axe) return;
        var speedRate = blade ? GeneHunterAttributeInit.BLADE_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT : GeneHunterAttributeInit.AXE_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT;
        var lostCritRate = blade ? GeneHunterAttributeInit.BLADE_LOST_CRITICAL_FIXED_DAMAGE_PER_PERCENT : GeneHunterAttributeInit.AXE_LOST_CRITICAL_FIXED_DAMAGE_PER_PERCENT;
        var flatRate = blade ? GeneHunterAttributeInit.BLADE_FIXED_DAMAGE_RATIO_PER_POINT : GeneHunterAttributeInit.AXE_FIXED_DAMAGE_RATIO_PER_POINT;
        double fixedDamage = Math.max(0.0D, -value(player, GeneHunterAttributeInit.CRITICAL_CHANCE) * 100.0D) * value(player, lostCritRate);
        double ratio = attackSpeedBonusPercent(player) * value(player, speedRate) + weaponFlatDamage(player, blade) * value(player, flatRate);
        if (fixedDamage > 0.0D || ratio > 0.0D) event.setNewDamage((float) ((event.getNewDamage() + fixedDamage) * (1.0D + ratio / 100.0D)));
    }
    private static double attackSpeedBonusPercent(ServerPlayer player) { AttributeInstance a = player.getAttribute(Attributes.ATTACK_SPEED); return a == null || a.getBaseValue() <= 0 ? 0 : Math.max(0, (a.getValue() / a.getBaseValue() - 1) * 100); }
    private static double weaponFlatDamage(ServerPlayer player, boolean blade) { return value(player, blade ? GeneHunterAttributeInit.BLADE_WEAPON_DAMAGE : GeneHunterAttributeInit.AXE_WEAPON_DAMAGE); }
    private static double value(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) { AttributeInstance a = player.getAttribute(attribute.holder()); return a == null ? 0 : a.getValue(); }
}
