package org.galaxy.gene_hunter.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Applies the first-hit bonus independently for every player-target pair. */
@EventBusSubscriber(modid = GeneHunter.MODID)
public final class FirstAttackDamageHandler {
    private static final int KILL_ENCHANT_ATTACKS = 3;
    private static final ResourceLocation KILL_ENCHANT_DAMAGE_ID = GeneHunter.asResource("kill_enchant_damage");
    private static final Map<UUID, Set<UUID>> ATTACKERS_BY_TARGET = new HashMap<>();
    private static final Map<UUID, KillEnchantState> KILL_ENCHANTS = new HashMap<>();

    private FirstAttackDamageHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        double damageRatio = 0.0D;
        if (markFirstAttack(player, event.getEntity())) {
            damageRatio += attributeValue(player, GeneHunterAttributeInit.FIRST_ATTACK_DAMAGE_RATIO);
        }
        if (damageRatio != 0.0D) {
            event.setNewDamage((float) (event.getNewDamage() * (1.0D + damageRatio)));
        }
    }

    @SubscribeEvent
    public static void onPlayerKill(LivingDeathEvent event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        applyKillEnchant(player, event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onEnchantedAttack(LivingDamageEvent.Post event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        KillEnchantState state = KILL_ENCHANTS.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (state.killingTarget() != null && state.killingTarget().equals(event.getEntity().getUUID())) {
            KILL_ENCHANTS.put(player.getUUID(), new KillEnchantState(state.remainingAttacks(), null));
            return;
        }

        int remainingAttacks = state.remainingAttacks() - 1;
        if (remainingAttacks <= 0) {
            removeKillEnchant(player);
        } else {
            KILL_ENCHANTS.put(player.getUUID(), new KillEnchantState(remainingAttacks, null));
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        ATTACKERS_BY_TARGET.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        KILL_ENCHANTS.remove(playerId);
        ATTACKERS_BY_TARGET.values().removeIf(attackers -> {
            attackers.remove(playerId);
            return attackers.isEmpty();
        });
    }

    private static boolean markFirstAttack(ServerPlayer player, Entity target) {
        return ATTACKERS_BY_TARGET
                .computeIfAbsent(target.getUUID(), ignored -> new HashSet<>())
                .add(player.getUUID());
    }

    private static void applyKillEnchant(ServerPlayer player, UUID killingTarget) {
        var attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        double damage = attributeValue(player, GeneHunterAttributeInit.KILL_ENCHANT_DAMAGE);
        attackDamage.removeModifier(KILL_ENCHANT_DAMAGE_ID);
        if (damage <= 0.0D) {
            KILL_ENCHANTS.remove(player.getUUID());
            return;
        }

        attackDamage.addTransientModifier(new AttributeModifier(
                KILL_ENCHANT_DAMAGE_ID,
                damage,
                AttributeModifier.Operation.ADD_VALUE
        ));
        KILL_ENCHANTS.put(player.getUUID(), new KillEnchantState(KILL_ENCHANT_ATTACKS, killingTarget));
    }

    private static void removeKillEnchant(ServerPlayer player) {
        var attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(KILL_ENCHANT_DAMAGE_ID);
        }
        KILL_ENCHANTS.remove(player.getUUID());
    }

    private static double attributeValue(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }

    private record KillEnchantState(int remainingAttacks, UUID killingTarget) {
    }
}
