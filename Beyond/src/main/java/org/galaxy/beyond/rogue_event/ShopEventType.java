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

/**
 * 商店事件 —— 在节点附近生成一个有职业的村民。
 */
public class ShopEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("shop");

    public ShopEventType() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        BlockPos nodePos = RogueSpawnHelper.nodeBase(level, context.nodePos());

        level.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(Component.translatable("beyond.event.shop.spawn")));

        BlockPos spawnPos = RogueSpawnHelper.findGroundNear(level, nodePos, nodePos.getY(), 2);
        if (spawnPos == null) return;

        var villager = EntityType.VILLAGER.create(level);
        if (villager == null) return;

        villager.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        villager.addTag("beyond_shop_event");
        villager.setPersistenceRequired();
        level.addFreshEntity(villager);
    }

    @Override
    @NonNull
    public Result next(Context context) {
        return Result.SUCCESS;
    }

}
