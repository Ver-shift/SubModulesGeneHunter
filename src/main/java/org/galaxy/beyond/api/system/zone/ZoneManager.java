package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Zone 数据管理 —— 只负责 zone 的增删改查，不做 cap 管理和事件分发。
 */
public class ZoneManager implements IZoneManager {

    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) return;

        var dimData = BeyondAPI.getBeyondDimensionData(serverLevel);
        if (dimData.getSafeZoneStructureData().getInitialized() < 1) return;

        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos worldPos = new BlockPos(chunk.getPos().x() << 4, 0, chunk.getPos().z() << 4);
        if (structureManager.hasAnyStructure(serverLevel, worldPos)) {
            addNodeZone(serverLevel, worldPos);
        }
    }

    private LevelZoneData getLZD(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(level).getLevelZoneData();
    }

    private void syncLevelData(ServerLevel level) {
        level.syncData(BeyondAttachmentInit.GLOBAL_DATA.get());
    }

    @Override
    public boolean addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType) {
        return getLZD(serverLevel).addZone(pos, zoneType);
    }

    @Override
    public void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center) {
        ChunkPos cc = ChunkPos.containing(center);
        int radius = chunkSize / 2;
        LevelZoneData lzd = getLZD(serverLevel);
        boolean changed = false;
        for (ChunkPos p : ZoneHelper.expandSquare(cc, radius)) {
            if (lzd.addZone(p, ZoneType.Safe_Zone)) changed = true;
        }
        if (changed) syncLevelData(serverLevel);
    }

    @Override
    public void addNodeZone(ServerLevel serverLevel, BlockPos pos) {
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = structureManager.getStructureChunks(serverLevel, pos);
        LevelZoneData lzd = getLZD(serverLevel);
        boolean changed = false;
        for (ChunkPos cp : chunks) {
            if (cp == null) continue;
            ZoneType existing = lzd.getZoneType(cp);
            if (existing != null && existing.matches(ZoneType.Safe_Zone.mask())) continue;
            if (lzd.addZone(cp, ZoneType.Node_Zone)) changed = true;
        }
        if (changed) {
            var rogueData = BeyondAPI.getBeyondDimensionData(serverLevel).getRogueData();
            var nd = new org.galaxy.beyond.api.system.node.NodeData(randomNodeColor(serverLevel));
            for (ChunkPos cp : chunks) {
                if (cp != null) nd.addChunkPos(cp);
            }
            if (!nd.getNodeChunks().isEmpty()) {
                rogueData.addNodeData(nd);
            }
            syncLevelData(serverLevel);
        }
    }

    private static final org.galaxy.beyond.api.system.node.NodeColor[] NODE_COLORS = {
            org.galaxy.beyond.api.system.node.NodeColor.GREEN,
            org.galaxy.beyond.api.system.node.NodeColor.ORANGE,
            org.galaxy.beyond.api.system.node.NodeColor.RED
    };
    private static org.galaxy.beyond.api.system.node.NodeColor randomNodeColor(ServerLevel level) {
        return NODE_COLORS[level.getRandom().nextInt(NODE_COLORS.length)];
    }

    @Override
    public void activeZoneInit(ServerLevel serverLevel) {
        LevelZoneData lzd = getLZD(serverLevel);
        Set<Map.Entry<ChunkPos, ZoneType>> entries = lzd.getZoneEntries();

        var safe = ZoneHelper.filterByMask(entries, ZoneType.Safe_Zone.mask());
        if (safe.isEmpty()) return;

        var cfg = BeyondAPI.getGlobalData(serverLevel).getRogueConfig();
        int minSteps = cfg.getMinNodeExpandChunks();
        int maxSteps = cfg.getMaxNodeExpandRange();
        int minNodes = cfg.getMinNodeExpandCount();

        Set<ChunkPos> allNodeChunks = entries.stream()
                .filter(e -> e.getValue().matches(ZoneType.Node_Zone.mask()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<ChunkPos> visited = entries.stream()
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        // BFS from all safe zone chunks
        Deque<ChunkPos> queue = new ArrayDeque<>();
        Map<ChunkPos, Integer> dist = new HashMap<>();
        for (ChunkPos seed : safe.chunks()) {
            queue.add(seed);
            dist.put(seed, 0);
        }
        Set<ChunkPos> added = new HashSet<>();

        // Expand in layers, checking node count after each layer
        int currentStep = 0;
        while (!queue.isEmpty() && currentStep <= maxSteps) {
            ChunkPos cur = queue.poll();
            int d = dist.get(cur);
            if (d != currentStep) {
                currentStep = d;
                if (currentStep >= minSteps && countNodeComponents(allNodeChunks, added) >= minNodes)
                    break;
            }
            if (d >= maxSteps) continue;

            for (ChunkPos nb : neighbors4(cur)) {
                if (visited.contains(nb)) continue;
                visited.add(nb);

                if (addZone(serverLevel, nb, ZoneType.Active_Zone)) {
                    added.add(nb);
                    dist.put(nb, d + 1);
                    queue.add(nb);
                }
            }
        }

        if (!added.isEmpty()) syncLevelData(serverLevel);
    }

    private static int countNodeComponents(Set<ChunkPos> allNodes, Set<ChunkPos> activeChunks) {
        Set<ChunkPos> remaining = new HashSet<>(allNodes);
        remaining.retainAll(activeChunks);
        int components = 0;
        while (!remaining.isEmpty()) {
            components++;
            floodFillNodes(remaining, remaining.iterator().next());
        }
        return components;
    }

    private static void floodFillNodes(Set<ChunkPos> remaining, ChunkPos seed) {
        Deque<ChunkPos> stack = new ArrayDeque<>();
        stack.push(seed);
        while (!stack.isEmpty()) {
            ChunkPos p = stack.pop();
            if (!remaining.remove(p)) continue;
            for (ChunkPos n : neighbors4(p)) stack.push(n);
        }
    }

    @Override
    public void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData) {
        List<ChunkPos> selfList = nodeData.getNodeData().getNodeChunkPosList();
        if (selfList.isEmpty()) return;

        int maxSteps = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        LevelZoneData lzd = getLZD(serverLevel);
        Set<ChunkPos> visited = lzd.getZoneEntries().stream()
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        boolean changed = false;

        Deque<ChunkPos> queue = new ArrayDeque<>();
        Map<ChunkPos, Integer> dist = new HashMap<>();
        for (ChunkPos seed : selfList) {
            queue.add(seed);
            dist.put(seed, 0);
        }

        while (!queue.isEmpty()) {
            ChunkPos cur = queue.poll();
            int d = dist.get(cur);
            if (d >= maxSteps) continue;

            for (ChunkPos nb : neighbors4(cur)) {
                if (visited.contains(nb)) continue;
                visited.add(nb);

                if (addZone(serverLevel, nb, ZoneType.Active_Zone)) {
                    changed = true;
                    dist.put(nb, d + 1);
                    queue.add(nb);
                }
            }
        }

        if (changed) syncLevelData(serverLevel);
    }

    private static List<ChunkPos> neighbors4(ChunkPos p) {
        int cx = p.getMinBlockX() >> 4;
        int cz = p.getMinBlockZ() >> 4;
        return List.of(
                new ChunkPos(cx + 1, cz),
                new ChunkPos(cx - 1, cz),
                new ChunkPos(cx, cz + 1),
                new ChunkPos(cx, cz - 1));
    }

}
