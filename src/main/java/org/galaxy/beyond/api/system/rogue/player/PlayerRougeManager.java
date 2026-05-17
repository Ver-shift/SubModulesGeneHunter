package org.galaxy.beyond.api.system.rogue.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondComponentInit;
import org.galaxy.beyond.api.init.BeyondItemInit;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.IPlayerRougeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;
import org.galaxy.beyond.api.system.rogue.core.RogueState;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.galaxy.beyond.component.ValueComp;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class PlayerRougeManager implements IPlayerRougeManager {

    private final IRogueManager rogueManager;
    private static final int DEFAULT_MAX_LIVES = 3;
    private static final int SPECTATOR_TIMEOUT_TICKS = 20 * 10; // 10秒

    public PlayerRougeManager(IRogueManager rogueManager) {
        this.rogueManager = rogueManager;
    }

    @Override
    public void tick(ServerPlayer player) {
        switch (getState(player)) {
            case LOBBY -> {}
            case PRE_ROGUE -> {}
            case ON_PROGRESS -> {}
            case PRE_NODE -> handlePreNode(player);
            case PRE_EVENT -> handlePreEvent(player);
            case ON_EVENT -> {}
            case SPECTATOR -> handleSpectator(player);
            case DEAD -> handleStateDeath(player);
            case REWARD -> handleReward(player);
            case PROGRESS_FINISH -> {}
        }
    }

    @Override
    public void intoRogue(ServerPlayer player) {
        BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().addRoguePlayer(player.getUUID());
        var data = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
        if (data.getMaxLifeCount() == 0) {
            data.setMaxLifeCount(DEFAULT_MAX_LIVES);
            data.setLifeCount(DEFAULT_MAX_LIVES);
        }
    }

    @Override
    public void leaveRogue(ServerPlayer player) {
        BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().removeRoguePlayer(player.getUUID());
        setState(player, PlayerRogueState.LOBBY);
    }

    @Override
    public void playerLevelSafeZone(ServerPlayer player) {
        giveItem(player, BeyondItemInit.LOOT_BAG.get());
    }

    @Override
    public void playerIntoSafeZone(ServerPlayer player) {
        int totalValue = clearPlayerInventoryWithValueComp(player);
        if (totalValue > 0) {
            player.sendSystemMessage(Component.translatable("beyond.info.back_to_safe_zone", totalValue));
        }
        setState(player, PlayerRogueState.LOBBY);
        leaveRogue(player);
    }

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        // 安全区 → 外部：检查冷却，发放战利品袋
        if (from == ZoneType.Safe_Zone && to != ZoneType.Safe_Zone) {
            if (isInRogue(player)) return;
            var data = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
            long now = player.level().getGameTime();
            long cooldownTicks = CommonConfig.LOBBY_COOLDOWN_SECONDS.get() * 20L;
            if (now - data.getLastLeaveSafeZoneTime() < cooldownTicks) {
                long remainingTicks = cooldownTicks - (now - data.getLastLeaveSafeZoneTime());
                long displaySeconds = (remainingTicks + 19) / 20;
                player.sendSystemMessage(Component.translatable("beyond.info.leave_too_frequent", displaySeconds));
                return;
            }
            intoRogue(player);
            playerLevelSafeZone(player);
            data.setLastLeaveSafeZoneTime(now);
            // 保持 LOBBY 状态，等待玩家使用战利品袋
        }
        // 外部 → 安全区：退出游戏
        if (from != ZoneType.Safe_Zone && to == ZoneType.Safe_Zone) {
            playerIntoSafeZone(player);
        }
    }

    @Override
    public void useLootBag(ServerPlayer player) {
        giveItem(player, Items.IRON_SWORD);
        setState(player, PlayerRogueState.PRE_ROGUE);

        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        int totalRogues = cfg.getRoguePlayerIds().size();
        int readyCount = countReadyPlayers(player);
        player.sendSystemMessage(Component.translatable("beyond.rogue.player_ready", readyCount, totalRogues));

        if (readyCount < totalRogues) {
            player.sendSystemMessage(Component.translatable("beyond.rogue.waiting_players", totalRogues - readyCount));
        }
    }

    private int countReadyPlayers(ServerPlayer self) {
        int count = 0;
        var overworld = BeyondAPI.getOverWorld();
        var cfg = BeyondAPI.getGlobalData(overworld).getRogueConfig();
        var playerList = overworld.getServer().getPlayerList();
        for (UUID id : cfg.getRoguePlayerIds()) {
            ServerPlayer p = playerList.getPlayer(id);
            if (p != null && getState(p) == PlayerRogueState.PRE_ROGUE) count++;
        }
        return count;
    }

    @Override
    public void clickNodeBlock(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        var nodeManager = rogueManager.getRogueNodeManager();
        NodeState nodeState = nodeManager.getNodeState(level);
        boolean solo = getRoguePlayerCount() == 1;

        switch (nodeState) {
            // ---- 分支1：LOCK → 激活节点 ----
            case LOCKED -> {
                nodeManager.setNodeState(level, NodeState.PRE_NODE);
                setState(player, PlayerRogueState.PRE_NODE);
                player.sendSystemMessage(Component.translatable("beyond.node.locked_triggered"));
                if (solo) {
                    nodeManager.setNodeState(level, NodeState.ON_EVENT);
                    player.sendSystemMessage(Component.translatable("beyond.node.solo_auto_start"));
                }
            }

            // ---- 分支2：PRE_NODE → 检查全员 ----
            case PRE_NODE -> {
                if (allRoguePlayersMatch(PlayerRogueState.PRE_NODE)) {
                    nodeManager.setNodeState(level, NodeState.ON_EVENT);
                    if (!solo) player.sendSystemMessage(Component.translatable("beyond.node.pre_node_all_ready"));
                } else if (!solo) {
                    int cnt = countRoguePlayersInState(PlayerRogueState.PRE_NODE);
                    int total = getRoguePlayerCount();
                    player.sendSystemMessage(Component.translatable("beyond.node.pre_node_waiting", cnt, total));
                }
            }

            // ---- 分支3：PRE_EVENT → 检查全员 ----
            case PRE_EVENT -> {
                if (allRoguePlayersMatch(PlayerRogueState.PRE_EVENT)) {
                    nodeManager.setNodeState(level, NodeState.ON_EVENT);
                    if (!solo) player.sendSystemMessage(Component.translatable("beyond.node.pre_event_all_ready"));
                } else if (!solo) {
                    int cnt = countRoguePlayersInState(PlayerRogueState.PRE_EVENT);
                    int total = getRoguePlayerCount();
                    player.sendSystemMessage(Component.translatable("beyond.node.pre_event_waiting", cnt, total));
                }
            }

            // ---- 分支4：ON_EVENT → 最后一个事件？ ----
            case ON_EVENT -> {
                var progressMgr = rogueManager.getProgressManager();
                int current = progressMgr.getCurrentProgressIndex(level);
                int total = progressMgr.getTotalProgress(level);
                if (total > 0 && current >= total - 1) {
                    nodeManager.setNodeState(level, NodeState.UNLOCKED);
                    progressMgr.advanceProgress(level);
                    player.sendSystemMessage(Component.translatable("beyond.node.unlocked"));
                } else {
                    nodeManager.setNodeState(level, NodeState.PRE_EVENT);
                    setAllRoguePlayersState(PlayerRogueState.PRE_EVENT);
                    if (!solo) {
                        player.sendSystemMessage(Component.translatable("beyond.node.next_event"));
                    }
                }
            }

            // ---- 分支5：UNLOCKED → 已解锁（沉默） ----
            case UNLOCKED -> { /* 不刷屏 */ }
        }
    }

    private boolean allRoguePlayersMatch(PlayerRogueState target) {
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        var playerList = BeyondAPI.getOverWorld().getServer().getPlayerList();
        var ids = cfg.getRoguePlayerIds();
        if (ids.isEmpty()) return false;
        for (UUID id : ids) {
            ServerPlayer p = playerList.getPlayer(id);
            if (p == null) return false;
            if (getState(p) != target) return false;
        }
        return true;
    }

    private int countRoguePlayersInState(PlayerRogueState target) {
        int count = 0;
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        var playerList = BeyondAPI.getOverWorld().getServer().getPlayerList();
        for (UUID id : cfg.getRoguePlayerIds()) {
            ServerPlayer p = playerList.getPlayer(id);
            if (p != null && getState(p) == target) count++;
        }
        return count;
    }

    private int getRoguePlayerCount() {
        return BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().getRoguePlayerIds().size();
    }

    private void setAllRoguePlayersState(PlayerRogueState target) {
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        var playerList = BeyondAPI.getOverWorld().getServer().getPlayerList();
        for (UUID id : cfg.getRoguePlayerIds()) {
            ServerPlayer p = playerList.getPlayer(id);
            if (p != null) setState(p, target);
        }
    }

    @Override
    public void playerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isInRogue(player)) {
            var rogueData = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
            int lives = rogueData.getLifeCount() - 1;
            rogueData.setLifeCount(lives);

            player.setHealth(player.getMaxHealth());
            event.setCanceled(true);

            if (lives <= 0) {
                rogueData.setLifeCount(0);
                rogueData.setSpectatorTicks(SPECTATOR_TIMEOUT_TICKS);
                setState(player, PlayerRogueState.SPECTATOR);
                player.sendSystemMessage(Component.translatable("beyond.rogue.death.no_lives"));
            } else {
                setState(player, PlayerRogueState.SPECTATOR);
                player.sendSystemMessage(Component.translatable("beyond.rogue.death.lives_left", lives));
            }
        }
    }

    @Override
    public void handlePreRogue(ServerPlayer player) {
        giveItem(player, Items.IRON_SWORD);
    }

    public void handlePreNode(ServerPlayer player) {
        // 发布当前准备人数和消息
    }

    public void handlePreEvent(ServerPlayer player) {
        // 发布当前准备人数和消息
    }

    public void handleSpectator(ServerPlayer player) {
        var rogueData = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
        int ticks = rogueData.getSpectatorTicks();

        if (rogueData.getLifeCount() > 0) {
            // 还有生命，立即复活
            setState(player, PlayerRogueState.ON_PROGRESS);
            rogueData.setSpectatorTicks(0);
            return;
        }

        // 无生命，倒计时
        ticks--;
        rogueData.setSpectatorTicks(ticks);

        if (ticks <= 0) {
            // 时间到，进入死亡结算
            setState(player, PlayerRogueState.DEAD);
        }
    }

    /**
     * 玩家死亡状态 —— 检查是否所有玩家都已死亡
     */
    public void handleStateDeath(ServerPlayer player) {
        var overworld = BeyondAPI.getOverWorld();
        var cfg = BeyondAPI.getGlobalData(overworld).getRogueConfig();
        var playerList = overworld.getServer().getPlayerList();

        // 如果所有在游戏中的玩家都处于 DEAD 或 SPECTATOR 状态 → 全局结算
        boolean allDead = true;
        for (UUID id : cfg.getRoguePlayerIds()) {
            ServerPlayer p = playerList.getPlayer(id);
            if (p == null) continue;
            var state = getState(p);
            if (state != PlayerRogueState.DEAD
                    && state != PlayerRogueState.SPECTATOR
                    && state != PlayerRogueState.REWARD
                    && state != PlayerRogueState.PROGRESS_FINISH) {
                allDead = false;
                break;
            }
        }

        if (allDead) {
            for (UUID id : cfg.getRoguePlayerIds()) {
                ServerPlayer p = playerList.getPlayer(id);
                if (p != null) setState(p, PlayerRogueState.REWARD);
            }
            BeyondAPI.getBeyondDimensionData(overworld).getRogueData()
                    .setRogueState(RogueState.ROGUE_PROGRESS_FINISH);
        }
    }

    public void handleReward(ServerPlayer player) {
        // 结算奖励
        int totalValue = clearPlayerInventoryWithValueComp(player);
        player.sendSystemMessage(Component.translatable("beyond.rogue.reward", totalValue));
        setState(player, PlayerRogueState.PROGRESS_FINISH);
    }

    @Override
    public void setState(ServerPlayer player, PlayerRogueState state) {
        var data = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
        if (data.getState() == state) return;
        data.setState(state);
    }

    @Override
    public PlayerRogueState getState(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getState();
    }

    @Override
    public void giveItem(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(BeyondComponentInit.ITEM_VALUE, new ValueComp(1));
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(player.level(), stack);
        }
    }

    @Override
    public boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().isRoguePlayer(player.getUUID());
    }

    @Override
    public int clearPlayerInventoryWithValueComp(ServerPlayer player) {
        int[] totalValue = {0};
        int[] itemCount = {0};

        // 玩家主背包 + 护甲 + 副手
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ValueComp.has(stack)) {
                totalValue[0] += ValueComp.get(stack);
                itemCount[0] += stack.getCount();
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        // Curios 饰品栏
        var curios = CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            curios.get().getCurios().forEach((slotId, stacksHandler) -> {
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (ValueComp.has(stack)) {
                        totalValue[0] += ValueComp.get(stack);
                        itemCount[0] += stack.getCount();
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        }

        if (itemCount[0] > 0) {
            player.sendSystemMessage(Component.translatable(
                    "beyond.info.clear_inventory", itemCount[0], totalValue[0]));
        }
        return totalValue[0];
    }
}
