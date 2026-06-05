package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

import java.util.ArrayList;
import java.util.List;

public class LootBagOpenEvent extends LevelEvent {

    private final ServerPlayer player;
    private final IRogueContext context;
    private final List<ItemStack> rewards = new ArrayList<>();

    public LootBagOpenEvent(ServerLevel level, ServerPlayer player, IRogueContext context) {
        super(level);
        this.player = player;
        this.context = context;
    }

    @Override
    public ServerLevel getLevel() {
        return (ServerLevel) super.getLevel();
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public IRogueContext getContext() {
        return context;
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }

    public void addReward(ItemStack stack) {
        if (!stack.isEmpty()) {
            rewards.add(stack.copy());
        }
    }

    public void clearRewards() {
        rewards.clear();
    }

    public boolean hasRewards() {
        return !rewards.isEmpty();
    }

    public static LootBagOpenEvent post(ServerLevel level, ServerPlayer player, IRogueContext context) {
        LootBagOpenEvent event = new LootBagOpenEvent(level, player, context);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }
}
