package org.galaxy.gene_hunter.api.system.temperature;

import net.minecraft.world.entity.Mob;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;

public final class MobTemperature {
    private MobTemperature() {
    }

    public static int get(Mob mob) {
        return mob.getData(GeneHunterAttachInit.MOB_TEMPERATURE).temperature();
    }

    public static void set(Mob mob, int temperature) {
        MobTemperatureData data = mob.getData(GeneHunterAttachInit.MOB_TEMPERATURE);
        data.setTemperature(temperature);
        mob.setData(GeneHunterAttachInit.MOB_TEMPERATURE, data);
    }

    public static void add(Mob mob, int amount) {
        MobTemperatureData data = mob.getData(GeneHunterAttachInit.MOB_TEMPERATURE);
        data.addTemperature(amount);
        mob.setData(GeneHunterAttachInit.MOB_TEMPERATURE, data);
    }

    public static void applyAttackEffect(Mob mob, int temperature, double coolingRate, double recoveryRate, double burnDamage, double coldSlowdown) {
        if (temperature == 0) {
            return;
        }

        MobTemperatureData data = mob.getData(GeneHunterAttachInit.MOB_TEMPERATURE);
        data.addTemperature(temperature);
        data.mergeEffect(coolingRate, recoveryRate, burnDamage, coldSlowdown);
        if (data.temperature() == 0) {
            data.clearEffect();
        }
        mob.setData(GeneHunterAttachInit.MOB_TEMPERATURE, data);
    }
}
