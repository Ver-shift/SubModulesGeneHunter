package org.galaxy.beyond.rogue_event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

import java.util.UUID;

/**
 * BOSS 事件 —— 在节点附近生成一只铁傀儡，击败后通关。
 */
public class BossEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("boss");

    private UUID bossId;

    public BossEventType() { super(ID); }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        BlockPos nodePos = context.nodePos();

        level.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(Component.translatable("beyond.event.boss.spawn")));

        BlockPos spawnPos = findSpawnableTop(level, nodePos);
        var golem = EntityType.IRON_GOLEM.create(level);
        if (golem == null) return;

        golem.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        golem.addTag("beyond_boss_event");
        if (golem instanceof Mob mob) mob.setPersistenceRequired();
        level.addFreshEntity(golem);
        bossId = golem.getUUID();
    }

    @Override
    public Result next(Context context) {
        if (bossId == null) return Result.FAILURE;
        var entity = context.level().getEntity(bossId);
        if (entity == null || !entity.isAlive()) {
            bossId = null;
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }

    private static BlockPos findSpawnableTop(Level level, BlockPos pos) {
        int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
        return new BlockPos(pos.getX(), y, pos.getZ());
    }
}
