package org.galaxy.beyond.zone_cap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.zone.CapType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneType;

public class AllNodeZoneCap extends ZoneCapType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":all_node_zone");
    private static final long ENTER_COOLDOWN_TICKS = 20 * 5;

    public AllNodeZoneCap() {
        super(ID);
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public CapType getCapType() { return CapType.NORMAL; }

    @Override
    public void playerRightClickBlock(ServerPlayer player, Block block) {}

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        if (to != ZoneType.Node_Zone) return;

        var data = player.getData(org.galaxy.beyond.api.init.BeyondAttachmentInit.PLAYER_DATA.get())
                .getPlayerRogueData();
        long now = player.level().getGameTime();
        if (now - data.getLastLeaveSafeZoneTime() < ENTER_COOLDOWN_TICKS) return;
        data.setLastLeaveSafeZoneTime(now);

        var rogueData = BeyondAPI.getBeyondDimensionData(player.level()).getRogueData();
        if (rogueData.getCompletedNodeChunks().contains(ChunkPos.containing(player.getOnPos()))) return;

        var nodeData = rogueData.getRogueNodeData();
        if (nodeData == null || nodeData.getNodeData() == null) return;

        player.sendSystemMessage(Component.translatable("beyond.node.enter_zone",
                Component.translatable("beyond.node.color." + nodeData.getNodeData().getColor().name().toLowerCase())));
    }
}
