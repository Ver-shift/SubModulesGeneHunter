package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.system.temperature.AttackTemperaturePayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = GeneHunter.MODID)
public final class AttackSlashHandler {
    private static final Map<UUID, Long> LAST_SWING_TICKS = new HashMap<>();

    private AttackSlashHandler() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            startSlash(player, event.getTarget());
        }
    }

    public static void startSlash(ServerPlayer player, Entity initialTarget) {
        long gameTime = player.level().getGameTime();
        if (LAST_SWING_TICKS.getOrDefault(player.getUUID(), Long.MIN_VALUE) == gameTime) {
            return;
        }
        LAST_SWING_TICKS.put(player.getUUID(), gameTime);

        double chance = AttackPropagationFactory.triggerChance(player, AttackPropagationFactory.Type.SLASH);
        double damage = attributeValue(player, GeneHunterAttributeInit.ATTACK_SLASH_DAMAGE);
        double maxDistance = attributeValue(player, GeneHunterAttributeInit.ATTACK_SLASH_DISTANCE);
        if (chance <= 0.0D || damage <= 0.0D || maxDistance <= 0.0D || player.getRandom().nextDouble() >= chance) {
            return;
        }

        Vec3 direction = horizontalDirection(player.getLookAngle());
        if (direction.equals(Vec3.ZERO)) {
            return;
        }

        SlashWaveEntity slash = new SlashWaveEntity(player.level(), player, direction, damage, maxDistance,
                AttackTemperaturePayload.from(player));
        slash.ignoreTarget(initialTarget);
        player.level().addFreshEntity(slash);
    }

    private static Vec3 horizontalDirection(Vec3 direction) {
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        return horizontal.lengthSqr() < 1.0E-6D ? Vec3.ZERO : horizontal.normalize();
    }

    private static double attributeValue(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }
}
