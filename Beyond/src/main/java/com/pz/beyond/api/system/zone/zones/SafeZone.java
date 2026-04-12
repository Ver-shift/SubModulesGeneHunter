package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;

/**
 * 安全区域
 */
public class SafeZone extends AbstractZone {

    public static final ResourceLocation SAFE_ZONE = Beyond.asResource("safe_zone");

    public SafeZone() {
        super(SAFE_ZONE);
    }
}
