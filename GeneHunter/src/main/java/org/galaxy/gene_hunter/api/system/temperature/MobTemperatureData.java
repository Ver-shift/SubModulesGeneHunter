package org.galaxy.gene_hunter.api.system.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class MobTemperatureData {
    public static final int MIN_TEMPERATURE = -300;
    public static final int MAX_TEMPERATURE = 300;
    public static final Codec<MobTemperatureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("temperature").forGetter(MobTemperatureData::temperature),
            Codec.DOUBLE.fieldOf("cooling_rate").forGetter(MobTemperatureData::coolingRate),
            Codec.DOUBLE.fieldOf("recovery_rate").forGetter(MobTemperatureData::recoveryRate),
            Codec.DOUBLE.fieldOf("burn_damage").forGetter(MobTemperatureData::burnDamage),
            Codec.DOUBLE.fieldOf("cold_slowdown").forGetter(MobTemperatureData::coldSlowdown)
    ).apply(instance, MobTemperatureData::new));

    private int temperature;
    private double coolingRate;
    private double recoveryRate;
    private double burnDamage;
    private double coldSlowdown;

    public MobTemperatureData() {
        this(0);
    }

    public MobTemperatureData(int temperature) {
        this(temperature, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public MobTemperatureData(int temperature, double coolingRate, double recoveryRate, double burnDamage, double coldSlowdown) {
        setTemperature(temperature);
        this.coolingRate = coolingRate;
        this.recoveryRate = recoveryRate;
        this.burnDamage = burnDamage;
        this.coldSlowdown = coldSlowdown;
    }

    public int temperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = Math.clamp(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE);
    }

    public void addTemperature(int amount) {
        setTemperature(temperature + amount);
    }

    public double coolingRate() {
        return coolingRate;
    }

    public double recoveryRate() {
        return recoveryRate;
    }

    public double burnDamage() {
        return burnDamage;
    }

    public double coldSlowdown() {
        return coldSlowdown;
    }

    public void mergeEffect(double coolingRate, double recoveryRate, double burnDamage, double coldSlowdown) {
        this.coolingRate = Math.max(this.coolingRate, coolingRate);
        this.recoveryRate = Math.max(this.recoveryRate, recoveryRate);
        this.burnDamage = Math.max(this.burnDamage, burnDamage);
        this.coldSlowdown = Math.max(this.coldSlowdown, coldSlowdown);
    }

    public void clearEffect() {
        coolingRate = 0.0D;
        recoveryRate = 0.0D;
        burnDamage = 0.0D;
        coldSlowdown = 0.0D;
    }
}
