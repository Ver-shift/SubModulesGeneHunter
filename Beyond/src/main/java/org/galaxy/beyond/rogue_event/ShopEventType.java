package org.galaxy.beyond.rogue_event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

/**
 * 商店事件 —— 在节点附近生成一个有职业的村民。
 */
public class ShopEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("shop");

    public ShopEventType() { super(ID); }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        BlockPos nodePos = context.nodePos();

        level.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(Component.translatable("beyond.event.shop.spawn")));

        BlockPos spawnPos = findSpawnableTop(level, nodePos);
        var villager = EntityType.VILLAGER.create(level);
        if (villager == null) return;

        villager.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        villager.addTag("beyond_shop_event");
        villager.setPersistenceRequired();
        level.addFreshEntity(villager);
    }

    @Override
    public Result next(Context context) {
        return Result.SUCCESS;
    }

    private static BlockPos findSpawnableTop(Level level, BlockPos pos) {
        int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
        return new BlockPos(pos.getX(), y, pos.getZ());
    }
}
