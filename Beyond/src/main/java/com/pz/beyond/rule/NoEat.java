package com.pz.beyond.rule;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.SafeZone;
import com.pz.beyond.api.system.rule.IZoneRule;
import net.minecraft.resources.ResourceLocation;

public class NoEat implements IZoneRule<SafeZone> {
    @Override
    public ResourceLocation getIdentifier() {
        return Beyond.asResource("no_eat");
    }


}
