package org.galaxy.beyond.rogue_event;

import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.RogueSpawnHelper;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.UUID;

/**
 * BOSS 事件 —— 在节点附近生成一只铁傀儡，击败后通关。
 */
public class BossEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("boss");

    private UUID bossId;

    public BossEventType() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        if (context.type().getSceneType() != SceneType.CLIMAX) {
            return;
        }
        ServerLevel level = context.level();
        BlockPos nodePos = RogueSpawnHelper.nodeBase(level, context.nodePos());

        level.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(Component.translatable("beyond.event.boss.spawn")));

        BlockPos spawnPos = RogueSpawnHelper.findGroundNear(level, nodePos, nodePos.getY(), 2);
        if (spawnPos == null) return;

        var golem = EntityType.IRON_GOLEM.create(level);
        if (golem == null) return;

        golem.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        golem.addTag("beyond_boss_event");
        golem.setPersistenceRequired();
        level.addFreshEntity(golem);
        bossId = golem.getUUID();
    }

    @Override
    @NonNull
    public Result next(Context context) {
        if (context.type().getSceneType() != SceneType.CLIMAX) {
            return Result.SUCCESS;
        }
        if (bossId == null) return Result.FAILURE;
        var entity = context.level().getEntity(bossId);
        if (entity == null || !entity.isAlive()) {
            bossId = null;
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }

    @Override
    public int auto() {
        return 20;
    }

}
