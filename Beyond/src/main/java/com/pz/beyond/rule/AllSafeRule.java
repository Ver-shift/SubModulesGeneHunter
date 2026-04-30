package com.pz.beyond.rule;

import com.pz.beyond.api.init.BeyondEffectInit;
import com.pz.beyond.api.system.rule.AbstractRule;
import com.pz.beyond.api.system.zone.ZoneType;
import com.pz.beyond.api.system.zone.zones.SafeZone;
import com.pz.beyond.api.tag.BeyondEntityTags;
import net.blay09.mods.waystones.block.WaystoneBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;

/**
 * 在安全区内，自带饱和和生命恢复
 */
public class AllSafeRule extends AbstractRule {

    private static final int SAFE_EFFECT_DURATION_TICKS = 23 * 10;

    public AllSafeRule(ResourceLocation identifier) {
        super(identifier);
    }

    @Override
    protected int getRuleValue() {
        return 10;
    }

    @Override
    protected RuleType getRuleType() {
        return RuleType.Natural;
    }

    @Override
    protected int getMaxLevel() {
        return 0;
    }

    @Override
    public void playerTick(ServerPlayer player, ZoneType zoneType) {
        if (zoneType instanceof SafeZone) {
            player.addEffect(new MobEffectInstance(
                BeyondEffectInit.SAFE_EFFECT,
                SAFE_EFFECT_DURATION_TICKS,
                0,
                true,
                false,
                true
            ));
        }
        super.playerTick(player, zoneType);
    }

    @Override
    public void mobTick(Mob mob, ZoneType zoneType) {
        // 安全区内，带敌对标签的生物直接秒杀（不可抵挡）
        if (zoneType instanceof SafeZone && mob.getType().is(BeyondEntityTags.HOSTILE)) {
            mob.hurt(mob.damageSources().genericKill(), Float.MAX_VALUE);
        }
        super.mobTick(mob, zoneType);
    }

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        if (from instanceof SafeZone fromZone){
            player.sendSystemMessage(Component.literal("你出去了安全区，所有规则都将被禁用"));
        }
        if (to instanceof SafeZone toZone){
            player.sendSystemMessage(Component.literal("你进入了安全区，所有规则都将被启用"));
        }
    }

    @Override
    public void playerRightClickBlock(ServerPlayer player, Block block) {
        if (block instanceof WaystoneBlock waystoneBlock){
            player.sendSystemMessage(Component.literal("你在安全区内右键了传送石，所有规则都将被启用"));
        }
    }
}
