package org.galaxy.beyond.api.system.zone;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.zone.core.IZoneCapEvent;

/**
 * 区域处理事情的能力。能够自由装卸
 */
public abstract class ZoneCapType implements IZoneCapEvent {

    private Identifier id;
    public ZoneCapType(Identifier id) {
        this.id = id;
    }

    abstract int getMaxLevel();
    abstract CapType getCapType();
}
