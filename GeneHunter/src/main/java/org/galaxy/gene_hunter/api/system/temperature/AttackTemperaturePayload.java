package org.galaxy.gene_hunter.api.system.temperature;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

/** 攻击发生时快照的温度效果数据，可随攻击传播到其他怪物。 */
public record AttackTemperaturePayload(int temperature, double coolingRate, double recoveryRate,
                                       double burnDamage, double coldSlowdown) {
    public static AttackTemperaturePayload from(ServerPlayer player) {
        return new AttackTemperaturePayload(
                Mth.floor(value(player, GeneHunterAttributeInit.ATTACK_TEMPERATURE)),
                value(player, GeneHunterAttributeInit.ATTACK_TEMPERATURE_COOLING_RATE),
                value(player, GeneHunterAttributeInit.ATTACK_TEMPERATURE_RECOVERY_RATE),
                value(player, GeneHunterAttributeInit.ATTACK_TEMPERATURE_BURN_DAMAGE),
                value(player, GeneHunterAttributeInit.ATTACK_TEMPERATURE_COLD_SLOWDOWN)
        );
    }

    public void apply(Mob mob) {
        MobTemperature.applyAttackEffect(mob, temperature, coolingRate, recoveryRate, burnDamage, coldSlowdown);
    }

    private static double value(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }
}
