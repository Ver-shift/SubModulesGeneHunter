package com.pz.beyond.rule;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.system.rule.AbstractRule;
import com.pz.beyond.api.system.zone.ZoneType;
import com.pz.beyond.api.system.zone.zones.NodeZone;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllNodeRule extends AbstractRule {

    public static final ResourceLocation ALL_NODE_RULE = Beyond.asResource("all_node_rule");
    public AllNodeRule() {
        super(ALL_NODE_RULE);
    }

    @Override
    protected int getRuleValue() {
        return 10;
    }

    @Override
    protected RuleType getRuleType() {
        return RuleType.Natural;
    }

    @Override
    protected int getMaxLevel() {
        return 0;
    }

    @Override
    public void mobTick(Mob mob, ZoneType zoneType) {
        super.mobTick(mob, zoneType);
    }

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        if (to instanceof NodeZone) {
            player.sendSystemMessage(Component.literal("你以进入节点区域"));
            BeyondAPI.getBeyondManager().getProgressManager().playerEnterNode(player);
            //todo 节点方块发光，并且呈现橙色。
        }
        if (from instanceof NodeZone) {
            player.sendSystemMessage(Component.literal("你以离开节点区域"));
            BeyondAPI.getBeyondManager().getProgressManager().playerLeaveNode(player);
        }
    }

    @Override
    public void playerRightClickBlock(ServerPlayer player, Block block) {
        if (block == Blocks.OBSIDIAN) {
            BeyondAPI.getBeyondManager().getProgressManager().rightClickCenter(player);
        }
    }
}
