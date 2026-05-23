package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * NeoForge 事件：玩家所在区域类型发生变化时发布到 EVENT_BUS。
 */
public class LivingChangeZoneEvent extends LivingEvent {

    private final ZoneType from;
    private final ZoneType to;

    public LivingChangeZoneEvent(LivingEntity entity,ZoneType from,ZoneType to) {
        super(entity);
        this.from = from;
        this.to = to;
    }


    public ZoneType getFrom() {
        return from;
    }

    public ZoneType getTo() {
        return to;
    }
}
