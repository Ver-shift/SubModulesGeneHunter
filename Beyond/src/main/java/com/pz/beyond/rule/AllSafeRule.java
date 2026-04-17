package com.pz.beyond.rule;

import com.pz.beyond.api.system.rule.AbstractRule;
import com.pz.beyond.api.system.zone.AbstractZone;
import com.pz.beyond.api.system.zone.ZoneData;
import com.pz.beyond.api.system.zone.zones.SafeZone;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AllSafeRule extends AbstractRule {


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
    public void playerTick(ServerPlayer player, AbstractZone<?> zoneType) {
        super.playerTick(player, zoneType);
    }

    @Override
    public void playerChangeZone(ServerPlayer player, AbstractZone<?> from, AbstractZone<?> to) {
        if (from instanceof SafeZone fromZone){
            player.sendSystemMessage(Component.literal("你出去了安全区，所有规则都将被禁用"));
        }


    }
}
