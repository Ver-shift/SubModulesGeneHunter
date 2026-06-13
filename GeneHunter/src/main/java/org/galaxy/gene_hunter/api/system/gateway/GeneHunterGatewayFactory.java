package org.galaxy.gene_hunter.api.system.gateway;

import dev.shadowsoffire.gateways.gate.BossEventSettings;
import dev.shadowsoffire.gateways.gate.GateRules;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms;
import dev.shadowsoffire.gateways.gate.StandardWaveEntity;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.spawn.SpawnEntry;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;

import java.util.ArrayList;
import java.util.List;

public class GeneHunterGatewayFactory {

    private static final int GATEWAY_COLOR = 0xAA1F1F;
    private static final double SPAWN_RANGE = 10.0D;
    private static final double LEASH_RANGE = 64.0D;
    private static final double SPACING = 16.0D;

    public Gateway create(SpawnDefinition definition, SpawnPlan plan) {
        List<Wave> waves = createWaves(definition, plan);
        return NormalGateway.builder()
                .size(plan.boss() ? Gateway.Size.LARGE : Gateway.Size.MEDIUM)
                .color(GATEWAY_COLOR)
                .spawnAlgorithm(SpawnAlgorithms.INWARD_SPIRAL)
                .rules(GateRules.builder()
                        .spawnRange(SPAWN_RANGE)
                        .leashRange(LEASH_RANGE)
                        .spacing(SPACING)
                        .allowDiscarding(true)
                        .allowDimChange(true)
                        .removeOnFailure(true)
                        .requiresNearbyPlayer(true)
                        .build())
                .bossSettings(new BossEventSettings(BossEventSettings.Mode.BOSS_BAR, false))
                .keyRewards(GeneHunterGatewayRewardFactory.create(plan))
                .waves(waves)
                .build();
    }

    private static List<Wave> createWaves(SpawnDefinition definition, SpawnPlan plan) {
        List<SpawnEntry> entries = plan.entries();
        if (entries.isEmpty()) {
            throw new IllegalStateException("Spawn plan has no entries: " + definition.getId());
        }

        List<Wave> waves = new ArrayList<>();
        for (int i = 0; i < plan.waveCount(); i++) {
            waves.add(createWave(definition, entries));
        }
        return List.copyOf(waves);
    }

    private static Wave createWave(SpawnDefinition definition, List<SpawnEntry> entries) {
        var builder = Wave.builder()
                .maxWaveTime(definition.getMaxWaveTime())
                .setupTime(definition.getSetupTime());
        for (SpawnEntry entry : entries) {
            builder.entity(createWaveEntity(entry));
        }
        return builder.build();
    }

    private static StandardWaveEntity createWaveEntity(SpawnEntry entry) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entry.getEntity());
        var builder = StandardWaveEntity.builder(type)
                .count(Math.max(1, entry.getCount()))
                .finalizeSpawn(true);
        applyAttributeModifiers(builder, type, entry);
        return builder.build();
    }

    private static void applyAttributeModifiers(StandardWaveEntity.Builder builder, EntityType<?> type, SpawnEntry entry) {
        if (entry.getHealth() > 0.0F) {
            builder.addModifier(healthModifier(type, entry.getHealth()));
        }
        if (entry.getAttackDamage() > 0.0D) {
            builder.addModifier(WaveModifier.AttributeModifier.create(
                    Attributes.ATTACK_DAMAGE,
                    AttributeModifier.Operation.ADD_VALUE,
                    additiveValue(type, Attributes.ATTACK_DAMAGE, entry.getAttackDamage())));
        }
        if (entry.getArmor() > 0.0D) {
            builder.addModifier(WaveModifier.AttributeModifier.create(
                    Attributes.ARMOR,
                    AttributeModifier.Operation.ADD_VALUE,
                    additiveValue(type, Attributes.ARMOR, entry.getArmor())));
        }
    }

    private static WaveModifier healthModifier(EntityType<?> type, float targetHealth) {
        double baseHealth = baseHealth(type);
        float multiplier = (float) (targetHealth / Math.max(1.0D, baseHealth) - 1.0D);
        return WaveModifier.AttributeModifier.create(
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                multiplier);
    }

    private static double baseHealth(EntityType<?> type) {
        return baseValue(type, Attributes.MAX_HEALTH, 20.0D);
    }

    private static float additiveValue(EntityType<?> type, Holder<Attribute> attribute, double targetValue) {
        return (float) (targetValue - baseValue(type, attribute, 0.0D));
    }

    @SuppressWarnings("unchecked")
    private static double baseValue(EntityType<?> type, Holder<Attribute> attribute, double fallback) {
        if (!DefaultAttributes.hasSupplier(type)) return fallback;
        var supplier = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) type);
        return supplier.hasAttribute(attribute) ? supplier.getBaseValue(attribute) : fallback;
    }
}
