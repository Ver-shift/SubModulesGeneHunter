package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 节点区域进入 Cap —— 玩家踏入节点区域时播报节点颜色。
 */
public class NodeZoneEnterCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("node_zone_enter");

    private static final long ENTER_COOLDOWN_TICKS = 20 * 5;

    public NodeZoneEnterCap() { super(ID); }

    @Override
    public void changeZone(LivingEntity entity, ZoneType from, ZoneType to, IRogueContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (to != ZoneType.Node_Zone) return;

        var data = player.getData(org.galaxy.beyond.api.init.BeyondAttachmentInit.PLAYER_DATA.get())
                .getPlayerRogueData();
        long now = player.level().getGameTime();
        if (now - data.getLastNodeEnterTime() < ENTER_COOLDOWN_TICKS) return;
        data.setLastNodeEnterTime(now);

        var rogueData = BeyondAPI.getBeyondDimensionData(player.level()).getRogueData();
        ChunkPos at = ChunkPos.containing(player.getOnPos());
        var nodeData = rogueData.findNodeData(at);
        if (nodeData == null) return;

        var colorName = nodeData.getColor().name().toLowerCase();

        if (nodeData.getPhase() == NodePhase.UNLOCKED) {
            player.sendSystemMessage(Component.translatable("beyond.node.enter_zone_unlocked",
                    Component.translatable("beyond.node.color." + colorName)));
        } else {
            player.sendSystemMessage(Component.translatable("beyond.node.enter_zone",
                    Component.translatable("beyond.node.color." + colorName)));
        }
    }
}
