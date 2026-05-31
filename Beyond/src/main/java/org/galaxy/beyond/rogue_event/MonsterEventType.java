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

import java.util.*;

/**
 * 怪物事件 —— 在节点附近召唤 4 只特殊 NBT 僵尸，全部击杀后推进。
 */
public class MonsterEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("monster");

    private final Set<UUID> spawnedZombies = new HashSet<>();
    private boolean castCalled;

    public MonsterEventType() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        BlockPos nodePos = RogueSpawnHelper.nodeBase(level, context.nodePos());

        var players = level.getServer().getPlayerList().getPlayers();
        players.forEach(p -> p.sendSystemMessage(Component.translatable("beyond.event.monster.spawn")));

        spawnedZombies.clear();
        int spawned = 0;
        for (int range = 1; range <= 5 && spawned < 4; range++) {
            for (int dx = -range; dx <= range && spawned < 4; dx++) {
                for (int dz = -range; dz <= range && spawned < 4; dz++) {
                    BlockPos spawnPos = nodePos.offset(dx, 0, dz);
                    BlockPos top = RogueSpawnHelper.findGroundNear(level, spawnPos, nodePos.getY(), 1);
                    if (top == null) continue;

                    var zombie = EntityType.ZOMBIE.create(level);
                    if (zombie == null) continue;

                    zombie.setPos(top.getX() + 0.5, top.getY(), top.getZ() + 0.5);
                    zombie.addTag("beyond_monster_event");
                    zombie.setPersistenceRequired();
                    level.addFreshEntity(zombie);
                    spawnedZombies.add(zombie.getUUID());
                    spawned++;
                }
            }
        }
        castCalled = spawned > 0;
    }

    @Override
    @NonNull
    public Result next(Context context) {
        if (!castCalled) return Result.FAILURE;
        ServerLevel level = context.level();

        // 移除已死亡/消失的僵尸 UUID
        spawnedZombies.removeIf(uuid -> {
            var entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        if (spawnedZombies.isEmpty()) return Result.SUCCESS;
        return Result.FAILURE;
    }

}
