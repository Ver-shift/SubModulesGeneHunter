package org.galaxy.beyond.api.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 生物所在区域类型变化事件。
 * <p>
 * 触发时机：Beyond 检测到 {@link LivingEntity} 从一个 {@link ZoneType} 进入另一个 ZoneType 时。
 * 玩家、怪物和其他 LivingEntity 都可能触发该事件，监听者应根据 {@link #getEntity()} 的实际类型过滤。
 * <p>
 * 该事件只表示区域状态已经变化，不可取消。需要阻止某个实体进入区域时，应在移动、传送或区域规则层处理。
 */
public class LivingChangeZoneEvent extends LivingEvent {

    private final ZoneType from;
    private final ZoneType to;

    public LivingChangeZoneEvent(LivingEntity entity, ZoneType from, ZoneType to) {
        super(entity);
        this.from = from;
        this.to = to;
    }

    /** 变化前区域类型。 */
    public ZoneType getFrom() {
        return from;
    }

    /** 变化后区域类型。 */
    public ZoneType getTo() {
        return to;
    }
}
