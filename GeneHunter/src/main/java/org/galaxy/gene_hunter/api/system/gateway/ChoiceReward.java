package org.galaxy.gene_hunter.api.system.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import org.galaxy.gene_hunter.api.system.choice.GeneHunterChoiceRewards;

import java.util.function.Consumer;

public record ChoiceReward(int raidValue, boolean boss) implements Reward {

    public static final Codec<ChoiceReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("raid_value", 0).forGetter(ChoiceReward::raidValue),
            Codec.BOOL.optionalFieldOf("boss", false).forGetter(ChoiceReward::boss)
    ).apply(instance, ChoiceReward::new));

    @Override
    public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> loot) {
        boolean rewarded = false;
        NodeColor nodeColor = currentNodeColor(level);
        for (ServerPlayer sp : level.players()) {
            if (BeyondAPI.getBeyondPlayerData(sp).getPlayerRogueData().getPhase() == PlayerPhase.ON_EVENT) {
                reward(sp, nodeColor);
                rewarded = true;
            }
        }
        if (!rewarded && summoner instanceof ServerPlayer sp) {
            reward(sp, nodeColor);
        }
    }

    private void reward(ServerPlayer player, NodeColor nodeColor) {
        if (boss) {
            GeneHunterChoiceRewards.boss(player, nodeColor);
            return;
        }
        GeneHunterChoiceRewards.normal(player, nodeColor);
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
