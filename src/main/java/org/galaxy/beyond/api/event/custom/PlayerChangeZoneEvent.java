package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * NeoForge 事件：玩家所在区域类型发生变化时发布到 EVENT_BUS。
 */
public class PlayerChangeZoneEvent extends PlayerEvent {

    private final ZoneType from;
    private final ZoneType to;

    public PlayerChangeZoneEvent(ServerPlayer player, ZoneType from, ZoneType to) {
        super(player);
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
