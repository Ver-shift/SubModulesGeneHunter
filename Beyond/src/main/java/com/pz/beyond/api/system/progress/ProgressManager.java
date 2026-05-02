package com.pz.beyond.api.system.progress;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.definition.ProgressDefinition;
import com.pz.beyond.api.system.node.NodeData;
import com.pz.beyond.api.system.node.NodeEventType;
import com.pz.beyond.api.system.node.NodeState;
import com.pz.beyond.api.system.node.RolledData;
import com.pz.beyond.api.system.node.StructureKey;
import com.pz.beyond.api.system.node.core.INodeEventType;
import com.pz.beyond.api.system.progress.core.IProgressManager;
import com.pz.beyond.api.system.progress.core.ProgressSession;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.galaxylib.api.init.GalaxyLibAttachInit;
import org.galaxylib.api.system.random.RandomManager;

import java.util.ArrayList;
import java.util.List;

public class ProgressManager implements IProgressManager {

    // 新增：tick 驱动的关卡会话管理器
    private final ProgressSession session = new ProgressSession(this);

    public ProgressSession getSession() {
        return session;
    }

    /** 每 tick 由 BeyondManager.levelTick() 调用 */
    public void tick(ServerLevel level) {
        session.tick(level);
    }

    @Override
    public void initProgress(ResourceLocation progressID){
        // 接口未传 level，BeyondLevelData 仅存于主世界，直接取 overworld
        ServerLevel level = overworld();
        if (level == null || progressID == null) {
            return;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return;
        }
        ProgressDefinition definition = beyondLevelData.getProgressDefinitions().get(progressID);
        if (definition == null) {
            Beyond.debugLog("[ProgressManager] initProgress skipped: definition not found for {}", progressID);
            return;
        }
        // 委托给 definition.translate：统一初始化 currentProgress / sceneTypes / 默认 nodes / 默认 currentNodeData
        SingleThreadedRandomSource random = progressRandom(level);
        ProgressData fresh = definition.translate(random);
        beyondLevelData.setProgressData(fresh);
    }

    @Override
    public void initNode(ServerLevel level,Long chunkKey) {
        BeyondLevelData data = BeyondAPI.getBeyondLevelData(level);

        ResourceLocation currentProgress = data.getProgressData().getCurrentProgress();
        ProgressDefinition progressDefinition = data.getProgressDefinitions().get(currentProgress);


        ProgressData progressData = data.getProgressData();
        SceneType sceneType = progressData.getSceneTypes().get(progressData.getIndex());
        // 方案 C：区块 → 节点 通过 chunkIndex 反查
        NodeData nodeData = progressData.getNodeByChunk(chunkKey);

        if (nodeData != null && sceneType != null){
            // 从 Level 上的 RandomManager 取 Progress 统一随机源，保证跨模块 / 跨存档一致性
            RandomManager randomManager = GalaxyLibAttachInit.getRandomManager(level);
            SingleThreadedRandomSource random = randomManager.getSeed(RandomManager.PROGRESS_RANDOM_ID);

            // 原地复用 currentNodeData 对象（RolledData.create 返回传入的第一个实参），避免重建引用
            RolledData rolled = RolledData.create(
                    progressData.getCurrentNodeData(),
                    nodeData,
                    sceneType,
                    progressDefinition,
                    random);
            progressData.setCurrentNodeData(rolled);
        }else {
            // nodeData / sceneType 缺失：保持 currentNodeData 为初始空态，交由上游处理
        }

    }

    @Override
    public void rightClickCenter(ServerPlayer player) {
         if (player == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level == null) {
            return;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return;
        }
        // 新增：Session 阶段守卫 —— 仅 IN_PROGRESS 可交互节点
        if (!session.canInteractWithNode()) {
            return;
        }
        ProgressData progressData = beyondLevelData.getProgressData();
        long chunkKey = player.chunkPosition().toLong();
        // 方案 C：区块 → 节点 通过 chunkIndex 反查
        NodeData nodeData = progressData.getNodeByChunk(chunkKey);
        if (nodeData == null) {
            // 该 chunk 不是节点，忽略
            return;
        }

        StructureKey nodeKey = nodeData.getStructureKey();
        if (!canActivateNode(level, progressData, nodeKey)) {
            Beyond.debugLog("[ProgressManager] rightClick blocked by active node: key={}", nodeKey);
            return;
        }

        // 根据节点状态机推进：LOCKED -> READY -> ON_EVENT -> COMPLETED
        NodeState state = nodeData.getNodeState();
        switch (state) {
            case LOCKED -> {
                Beyond.debugLog("[ProgressManager] rightClick ignored (LOCKED): key={}", nodeKey);
            }
            case READY -> {
                // READY -> ON_EVENT：此时才 roll 事件
                Beyond.debugLog("[ProgressManager] state READY->ON_EVENT: key={}", nodeKey);
                setActiveKey(progressData, nodeKey);
                initNode(level, chunkKey);
                nodeData.setNodeState(NodeState.ON_EVENT);
                int before = safeEventIndex(progressData.getCurrentNodeData());
                eventHandle(progressData.getCurrentNodeData());
                int after = safeEventIndex(progressData.getCurrentNodeData());
                finishIfComplete(progressData, nodeData, nodeKey, after > before);
            }
            case ON_EVENT -> {
                // 推进一个事件
                Beyond.debugLog("[ProgressManager] state ON_EVENT trigger: key={}", nodeKey);
                setActiveKey(progressData, nodeKey);
                int before = safeEventIndex(progressData.getCurrentNodeData());
                eventHandle(progressData.getCurrentNodeData());
                int after = safeEventIndex(progressData.getCurrentNodeData());
                finishIfComplete(progressData, nodeData, nodeKey, after > before);
            }
            case COMPLETED -> {
                Beyond.debugLog("[ProgressManager] rightClick ignored (COMPLETED): key={}", nodeKey);
                // 已完成，不再触发
                clearActiveIfMatches(progressData, nodeKey);
            }
        }
    }

    public void playerEnterNode(ServerPlayer player) {
        if (player == null) {
            return;
        }
        // 新增：Session 阶段守卫 —— 仅 IN_PROGRESS 可交互节点
        if (!session.canInteractWithNode()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level == null) {
            return;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return;
        }
        ProgressData progressData = beyondLevelData.getProgressData();
        long chunkKey = player.chunkPosition().toLong();
        NodeData nodeData = progressData.getNodeByChunk(chunkKey);
        if (nodeData == null) {
            return;
        }

        StructureKey nodeKey = nodeData.getStructureKey();
        releaseActiveIfNoPlayers(level, progressData);
        if (!canActivateNode(level, progressData, nodeKey)) {
            Beyond.debugLog("[ProgressManager] enter blocked by active node: key={}", nodeKey);
            return;
        }
        if (nodeData.getNodeState() == NodeState.COMPLETED) {
            return;
        }
        nodeData.setNodeState(NodeState.READY);
        setActiveKey(progressData, nodeKey);
        Beyond.debugLog("[ProgressManager] state LOCKED->READY: key={}", nodeKey);
    }

    public void playerLeaveNode(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level == null) {
            return;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return;
        }
        ProgressData progressData = beyondLevelData.getProgressData();
        releaseActiveIfNoPlayers(level, progressData);
    }

    @Override
    public void eventHandle(RolledData data) {
        if (data == null || data.getEvents() == null) {
            return;
        }
        List<NodeEventType> events = data.getEvents().getEvents();
        if (events == null || events.isEmpty()) {
            return;
        }
        int index = data.getCurrentEventIndex();
        if (index < 0 || index >= events.size()) {
            return;
        }
        NodeEventType event = events.get(index);
        if (event == null) {
            return;
        }
        ServerLevel level = overworld();
        if (level == null) {
            return;
        }
        Beyond.debugLog("[ProgressManager] eventHandle try: index={}/{} type={}", index, events.size(), event.getIdentifier());
        // 事件触发上下文：主世界内所有在线玩家
        List<ServerPlayer> players = new ArrayList<>(level.players());
        INodeEventType.Context context = new INodeEventType.Context(players, level, data);

        INodeEventType.Result result = event.canNextEvent(context);
        if (result != null && result.isSuccuss()) {
            event.cast(context);
            data.setCurrentEventIndex(index + 1);
            Beyond.debugLog("[ProgressManager] eventHandle success: index={}->{}", index, data.getCurrentEventIndex());
        } else {
            Beyond.debugLog("[ProgressManager] eventHandle blocked: index={} result={}", index, result);
        }
    }

    @Override
    public void chunkLoad(ServerLevel level) {

    }

    // --- helpers ---

    private static ServerLevel overworld() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.overworld();
    }

    private static SingleThreadedRandomSource progressRandom(ServerLevel level) {
        RandomManager rm = GalaxyLibAttachInit.getRandomManager(level);
        return rm == null ? null : rm.getSeed(RandomManager.PROGRESS_RANDOM_ID);
    }

    private static void setActiveKey(ProgressData progressData, StructureKey key) {
        if (progressData.getCurrentNodeData() == null) {
            progressData.setCurrentNodeData(new RolledData());
        }
        progressData.getCurrentNodeData().setStructureKey(key == null ? StructureKey.EMPTY : key);
    }

    private static StructureKey getActiveKey(ProgressData progressData) {
        if (progressData == null || progressData.getCurrentNodeData() == null) {
            return StructureKey.EMPTY;
        }
        StructureKey key = progressData.getCurrentNodeData().getStructureKey();
        return key == null ? StructureKey.EMPTY : key;
    }

    private static boolean hasPlayerInNode(ServerLevel level, ProgressData progressData, StructureKey key) {
        if (level == null || progressData == null || key == null || key == StructureKey.EMPTY) {
            return false;
        }
        for (ServerPlayer p : level.players()) {
            if (p == null) {
                continue;
            }
            StructureKey playerKey = progressData.getStructureKeyByChunk(p.chunkPosition().toLong());
            if (key.equals(playerKey)) {
                return true;
            }
        }
        return false;
    }

    private static void releaseActiveIfNoPlayers(ServerLevel level, ProgressData progressData) {
        StructureKey activeKey = getActiveKey(progressData);
        if (activeKey == StructureKey.EMPTY) {
            return;
        }
        NodeData nodeData = progressData.getNodes().get(activeKey);
        if (nodeData == null) {
            setActiveKey(progressData, StructureKey.EMPTY);
            return;
        }
        if (nodeData.getNodeState() == NodeState.COMPLETED) {
            setActiveKey(progressData, StructureKey.EMPTY);
            return;
        }
        if (!hasPlayerInNode(level, progressData, activeKey) && nodeData.getNodeState() == NodeState.READY) {
            nodeData.setNodeState(NodeState.LOCKED);
            setActiveKey(progressData, StructureKey.EMPTY);
            Beyond.debugLog("[ProgressManager] state READY->LOCKED (no players): key={}", activeKey);
        }
    }

    private static boolean canActivateNode(ServerLevel level, ProgressData progressData, StructureKey nodeKey) {
        if (nodeKey == null || nodeKey == StructureKey.EMPTY) {
            return false;
        }
        releaseActiveIfNoPlayers(level, progressData);
        StructureKey activeKey = getActiveKey(progressData);
        return activeKey == StructureKey.EMPTY || activeKey.equals(nodeKey);
    }

    private static void finishIfComplete(ProgressData progressData, NodeData nodeData, StructureKey nodeKey, boolean advanced) {
        if (!advanced) {
            return;
        }
        RolledData rolled = progressData.getCurrentNodeData();
        if (rolled != null && rolled.getEvents() != null) {
            List<NodeEventType> list = rolled.getEvents().getEvents();
            if (list != null && rolled.getCurrentEventIndex() >= list.size()) {
                nodeData.setNodeState(NodeState.COMPLETED);
                Beyond.debugLog("[ProgressManager] state ON_EVENT->COMPLETED: key={}", nodeKey);
                clearActiveIfMatches(progressData, nodeKey);
            }
        }
    }

    private static int safeEventIndex(RolledData data) {
        return data == null ? -1 : data.getCurrentEventIndex();
    }

    private static void clearActiveIfMatches(ProgressData progressData, StructureKey nodeKey) {
        StructureKey activeKey = getActiveKey(progressData);
        if (activeKey.equals(nodeKey)) {
            setActiveKey(progressData, StructureKey.EMPTY);
        }
    }

    public StructureKey resetActiveNode(ServerLevel level) {
        if (level == null) {
            return null;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return null;
        }
        ProgressData progressData = beyondLevelData.getProgressData();
        StructureKey activeKey = getActiveKey(progressData);
        if (activeKey == StructureKey.EMPTY) {
            Beyond.debugLog("[ProgressManager] reset skipped: no active node");
            return null;
        }
        NodeData nodeData = progressData.getNodes().get(activeKey);
        if (nodeData == null) {
            progressData.setCurrentNodeData(new RolledData());
            Beyond.debugLog("[ProgressManager] reset skipped: node missing, key={}", activeKey);
            return null;
        }
        nodeData.setNodeState(NodeState.LOCKED);
        progressData.setCurrentNodeData(new RolledData());
        Beyond.debugLog("[ProgressManager] reset node to LOCKED: key={}", activeKey);
        return activeKey;
    }
}
