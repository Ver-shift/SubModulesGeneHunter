package org.galaxylib.api.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class EntityMoveEvent extends LivingEvent {
    public EntityMoveEvent(LivingEntity entity) {
        super(entity);
    }
}
