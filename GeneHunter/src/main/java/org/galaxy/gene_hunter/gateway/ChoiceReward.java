package org.galaxy.gene_hunter.gateway;

import com.mojang.serialization.Codec;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Reward;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.gene_hunter.container.ChoiceContainer;

import java.util.function.Consumer;

public record ChoiceReward() implements Reward {

    public static final Codec<ChoiceReward> CODEC = Codec.unit(ChoiceReward::new);

    @Override
    public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> loot) {
        for (ServerPlayer sp : level.players()) {
            if (BeyondAPI.getBeyondPlayerData(sp).getPlayerRogueData().getPhase() == PlayerPhase.ON_EVENT) {
                ChoiceContainer.rogueRewardEvent(sp, currentNodeColor(level));
            }
        }
    }

    private NodeColor currentNodeColor(ServerLevel level) {
        var nodeData = BeyondAPI.getRogueData(level).getRogueNodeData();
        if (nodeData == null || nodeData.getNodeData() == null) {
            return NodeColor.GREEN;
        }
        return nodeData.getNodeData().getColor();
    }

    @Override
    public void appendHoverText(Item.TooltipContext ctx, Consumer<MutableComponent> tooltip) {
    }

    @Override
    public Codec<? extends Reward> getCodec() {
        return CODEC;
    }
}
