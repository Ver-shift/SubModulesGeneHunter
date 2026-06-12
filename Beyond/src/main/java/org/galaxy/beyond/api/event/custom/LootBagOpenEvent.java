package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 战利品袋打开事件。
 * <p>
 * 触发时机：玩家在肉鸽流程中打开 Beyond 的战利品袋时，默认奖励发放前发布。
 * 监听者可以通过 {@link #addReward(ItemStack)} 添加奖励，也可以用 {@link #clearRewards()}
 * 清空之前监听者加入的奖励后重建奖励列表。
 * <p>
 * 事件运行在服务端线程，{@link #getRewards()} 返回的是可变列表。加入列表的 ItemStack 会被复制，
 * 因此调用方不需要保留原始栈对象。
 */
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

    /** 打开奖励袋的玩家。 */
    public ServerPlayer getPlayer() {
        return player;
    }

    /** 当前肉鸽上下文，可用于读取参与玩家、关卡数据和阶段状态。 */
    public IRogueContext getContext() {
        return context;
    }

    /** 当前将要发放的奖励列表。 */
    public List<ItemStack> getRewards() {
        return rewards;
    }

    /** 添加一个非空奖励物品。 */
    public void addReward(ItemStack stack) {
        if (!stack.isEmpty()) {
            rewards.add(stack.copy());
        }
    }

    /** 清空当前事件已经收集到的奖励。 */
    public void clearRewards() {
        rewards.clear();
    }

    /** 是否已经有监听者加入奖励。 */
    public boolean hasRewards() {
        return !rewards.isEmpty();
    }

    public static LootBagOpenEvent post(ServerLevel level, ServerPlayer player, IRogueContext context) {
        LootBagOpenEvent event = new LootBagOpenEvent(level, player, context);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }
}
