package com.pz.beyond.rule;

import com.pz.beyond.api.system.rule.AbstractRule;
import com.pz.beyond.api.system.zone.AbstractZone;
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
    public void playerTick(ServerPlayer player, AbstractZone zoneType) {
        super.playerTick(player, zoneType);
    }
}
