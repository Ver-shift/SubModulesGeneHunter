package org.galaxy.beyond.rogue_event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

import java.util.*;

/**
 * 怪物事件 —— 在节点附近召唤 4 只特殊 NBT 僵尸，全部击杀后推进。
 */
public class MonsterEventType extends RogueEventType {

    public static final Identifier ID = Beyond.asResource("monster");

    private final Set<UUID> spawnedZombies = new HashSet<>();
    private boolean castCalled;

    public MonsterEventType() { super(ID); }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        BlockPos nodePos = context.nodePos();

        var players = level.getServer().getPlayerList().getPlayers();
        players.forEach(p -> p.sendSystemMessage(Component.translatable("beyond.event.monster.spawn")));

        castCalled = true;
        int spawned = 0;
        for (int range = 1; range <= 5 && spawned < 4; range++) {
            for (int dx = -range; dx <= range && spawned < 4; dx++) {
                for (int dz = -range; dz <= range && spawned < 4; dz++) {
                    BlockPos spawnPos = nodePos.offset(dx, 0, dz);
                    BlockPos top = findSpawnableTop(level, spawnPos);
                    if (top == null) continue;

                    var zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.EVENT);
                    if (zombie == null) continue;

                    zombie.setPos(top.getX() + 0.5, top.getY(), top.getZ() + 0.5);
                    zombie.addTag("beyond_monster_event");
                    if (zombie instanceof Mob mob) mob.setPersistenceRequired();
                    level.addFreshEntity(zombie);
                    spawnedZombies.add(zombie.getUUID());
                    spawned++;
                }
            }
        }
    }

    @Override
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

    /** 返回 pos 处最顶上的非空气方块上方一格 */
    private static BlockPos findSpawnableTop(Level level, BlockPos pos) {
        int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
        return new BlockPos(pos.getX(), y, pos.getZ());
    }
}
