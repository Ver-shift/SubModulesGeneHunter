package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 节点区域进入 Cap —— 玩家踏入节点区域时播报节点颜色。
 */
public class NodeZoneEnterCap extends RogueCap {

    public static final ResourceLocation ID = Beyond.asResource("node_zone_enter");

    private static final long ENTER_COOLDOWN_TICKS = 20 * 5;

    public NodeZoneEnterCap() { super(ID); }

    @Override
    public void changeZone(LivingEntity entity, ZoneType from, ZoneType to, IRogueContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (to != ZoneType.Node_Zone) return;

        var data = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
        long now = player.level().getGameTime();
        if (now - data.getLastNodeEnterTime() < ENTER_COOLDOWN_TICKS) return;
        data.setLastNodeEnterTime(now);

        ChunkPos at = org.galaxy.beyond.api.util.CompatUtil.chunkPos(player.blockPosition());
        var nodeData = BeyondAPI.findNodeData(player.level(), at);
        if (nodeData == null) return;

        var colorName = nodeData.getColor().name().toLowerCase();
        var color = Component.translatable("beyond.node.color." + colorName);

        if (BeyondAPI.getRogueData(player.level()).getPhase() == RoguePhase.LOBBY) {
            player.sendSystemMessage(Component.translatable("beyond.node.enter_zone_not_started", color));
            return;
        }

        if (nodeData.getPhase() == NodePhase.UNLOCKED) {
            player.sendSystemMessage(Component.translatable("beyond.node.enter_zone_unlocked", color));
        } else {
            player.sendSystemMessage(Component.translatable("beyond.node.enter_zone", color));
        }
    }
}
