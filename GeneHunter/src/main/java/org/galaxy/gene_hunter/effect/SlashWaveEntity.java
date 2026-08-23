package org.galaxy.gene_hunter.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.galaxy.gene_hunter.api.system.temperature.AttackTemperaturePayload;

/** Server-authoritative, client-rendered expanding slash wave. */
public final class SlashWaveEntity extends Entity {
    private static final double SPEED_PER_TICK = 1.0D;
    private static final double HIT_THICKNESS = 0.75D;
    private static final double MAX_CONE_WIDTH = 3.0D;
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.FLOAT);

    private final Set<UUID> hitTargets = new HashSet<>();
    private double damage;
    private double maxDistance;
    private double travelledDistance;
    private UUID ownerId;
    private AttackTemperaturePayload temperature;

    public SlashWaveEntity(EntityType<? extends SlashWaveEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public SlashWaveEntity(Level level, LivingEntity owner, Vec3 direction, double damage, double maxDistance,
                           AttackTemperaturePayload temperature) {
        this(org.galaxy.gene_hunter.api.init.GeneHunterEntityInit.SLASH_WAVE.get(), level);
        ownerId = owner.getUUID();
        setPos(owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D));
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        setDeltaMovement(direction.scale(SPEED_PER_TICK));
        this.damage = damage;
        this.maxDistance = maxDistance;
        this.temperature = temperature;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 0.5F);
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public void ignoreTarget(Entity target) {
        if (target != null) {
            hitTargets.add(target.getUUID());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        double previousDistance = travelledDistance;
        travelledDistance = Math.min(travelledDistance + SPEED_PER_TICK, maxDistance);
        entityData.set(RADIUS, (float) (coneWidthAt(travelledDistance) * 0.5D));
        damageTargets(previousDistance);
        if (travelledDistance >= maxDistance) {
            discard();
            return;
        }
        setPos(position().add(getDeltaMovement()));
    }

    private void damageTargets(double previousDistance) {
        if (ownerId == null || !(level().getPlayerByUUID(ownerId) instanceof ServerPlayer attacker)) {
            discard();
            return;
        }

        Vec3 direction = getDeltaMovement().normalize();
        Vec3 right = new Vec3(-direction.z, 0.0D, direction.x);
        double searchRadius = travelledDistance + HIT_THICKNESS;
        AABB searchArea = new AABB(attacker.position(), attacker.position()).inflate(searchRadius, 3.0D, searchRadius);
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity != attacker && entity.isAlive() && !hitTargets.contains(entity.getUUID()))) {
            if (attacker.isAlliedTo(target) || target instanceof Player targetPlayer
                    && attacker instanceof Player player && !player.canHarmPlayer(targetPlayer)) {
                continue;
            }
            Vec3 offset = target.position().subtract(attacker.position());
            Vec3 horizontalOffset = new Vec3(offset.x, 0.0D, offset.z);
            double targetDistance = horizontalOffset.length();
            if (targetDistance < previousDistance - HIT_THICKNESS || targetDistance > travelledDistance + HIT_THICKNESS
                    || horizontalOffset.lengthSqr() < 1.0E-6D) {
                continue;
            }
            double lateralDistance = Math.abs(horizontalOffset.dot(right));
            if (lateralDistance > coneWidthAt(targetDistance) * 0.5D + HIT_THICKNESS) {
                continue;
            }
            target.hurt(attacker.damageSources().mobAttack(attacker), (float) damage);
            if (target instanceof net.minecraft.world.entity.Mob mob && temperature != null) {
                temperature.apply(mob);
            }
            hitTargets.add(target.getUUID());
        }
    }

    /** Matches Gust's growing cone, capped at its widest three-block collider. */
    private static double coneWidthAt(double distance) {
        return Math.min(MAX_CONE_WIDTH, 0.5D + distance * 0.625D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage = tag.getDouble("Damage");
        maxDistance = tag.getDouble("MaxDistance");
        travelledDistance = tag.getDouble("TravelledDistance");
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("Damage", damage);
        tag.putDouble("MaxDistance", maxDistance);
        tag.putDouble("TravelledDistance", travelledDistance);
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }
}
