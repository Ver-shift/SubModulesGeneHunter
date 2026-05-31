package org.galaxy.gene_hunter.rogue_event;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.RogueSpawnHelper;

public abstract class GatewayEvent extends RogueEventType {

    public GatewayEvent(ResourceLocation id) {
        super(id);
    }

    protected abstract ResourceLocation gatewayId();

    @Override
    public void cast(Context context) {
        var level = context.level();
        var players = context.rogueContext().playersInRogue(level);
        if (players.isEmpty()) return;

        var entity = gateway().createEntity(level, players.getFirst());
        entity.setPos(RogueSpawnHelper.gatewayPos(level, context.nodePos(), 3));
        level.addFreshEntity(entity);
    }

    @Override
    @NonNull
    public Result next(Context context) {
        var range = gateway().rules().leashRange();
        var pos = context.nodePos().getCenter();
        var entities = context.level().getEntitiesOfClass(
                GatewayEntity.class,
                AABB.ofSize(pos, range, range, range));
        return entities.isEmpty() ? Result.SUCCESS : Result.FAILURE;
    }

    private Gateway gateway() {
        var id = gatewayId();
        return GatewayRegistry.INSTANCE.holder(id).getOptional()
                .orElseThrow(() -> new IllegalStateException("Missing gateway definition: " + id));
    }
}
