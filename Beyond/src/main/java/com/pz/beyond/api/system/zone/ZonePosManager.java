package com.pz.beyond.api.system.zone;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.node.NodeData;
import com.pz.beyond.api.system.node.NodeState;
import com.pz.beyond.api.system.node.StructureKey;
import com.pz.beyond.api.system.progress.ProgressData;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.structure.StructureData;
import com.pz.beyond.api.system.zone.core.IZonePosManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 区域坐标管理器：负责维护 {@link LevelZoneData#getZonePos()} 与 {@link LevelZoneData#getZoneData()}。
 * 纯算法层，不持有状态，由 {@code BeyondManager} 按 {@link ServerLevel} 调度。
 */
public class ZonePosManager implements IZonePosManager {

    /** 默认安全区方形边长（区块数），后续可迁移至 beyond-server.toml */
    public static final int DEFAULT_SAFE_CHUNK_SIZE = 11;

    // ==================== 接口实现 ====================

    @Override
    public void addSafeZone(ServerLevel level, int chunkCountSize) {
        if (chunkCountSize <= 0) {
            return;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        StructureData structureData = beyondLevelData.getStructureData();
        if (!structureData.hasValidSpawnPos()) {
            Beyond.debugLog("[ZonePosManager] SafeZone init skipped: spawnPos not ready.");
            return;
        }

        LevelZoneData zoneData = beyondLevelData.getLevelZoneData();
        ZoneType safeZone = BeyondZoneInit.SAFE_ZONE.get();
        ZoneType pending = BeyondZoneInit.PENDING_PLAYER_ACTIVE_ZONE.get();

        ChunkPos center = new ChunkPos(new BlockPos(
                structureData.getSpawnPos().getX(),
                structureData.getSpawnPos().getY(),
                structureData.getSpawnPos().getZ()
        ));
        int radius = chunkCountSize / 2;

        // 1) 以 center 为中心铺设新的方形 SafeZone
        Set<Long> targetSafeChunks = new HashSet<>();
        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                ChunkPos cp = new ChunkPos(x, z);
                targetSafeChunks.add(cp.toLong());
                markChunk(zoneData, cp, safeZone, level);
            }
        }

        // 2) 收缩：原来是 SAFE 但不在新方形内的 chunk，回落为 PENDING
        Map<Long, ZoneType> zonePos = zoneData.getZonePos();
        List<Long> shrinkKeys = new ArrayList<>();
        for (Map.Entry<Long, ZoneType> entry : zonePos.entrySet()) {
            if (entry.getValue() == safeZone && !targetSafeChunks.contains(entry.getKey())) {
                shrinkKeys.add(entry.getKey());
            }
        }
        for (Long key : shrinkKeys) {
            markChunk(zoneData, new ChunkPos(key), pending, level);
        }

        Beyond.debugLog("[ZonePosManager] SafeZone built: center={}, size={}, total={} chunks",
                center, chunkCountSize, targetSafeChunks.size());
    }

    @Override
    public void spawnZone(ServerLevel level, ChunkPos chunkPos) {
        LevelZoneData zoneData = BeyondAPI.getBeyondLevelData(level).getLevelZoneData();
        ZoneType current = zoneData.getZonePos().get(chunkPos.toLong());
        ZoneType empty = BeyondZoneInit.EMPTY;
        ZoneType pending = BeyondZoneInit.PENDING_PLAYER_ACTIVE_ZONE.get();

        // 已被 Safe/Active/Node 占用则跳过
        if (current != null && current != empty && current != pending) {
            return;
        }

        // TODO: 目前把所有地表结构都当作节点区域；待节点结构注册表落地后再过滤村庄/要塞等非节点结构
        List<StructureStart> surfaceStarts = collectSurfaceStructureStarts(level, chunkPos);
        if (!surfaceStarts.isEmpty()) {
            ZoneType nodeZone = BeyondZoneInit.NODE_ZONE.get();
            // 方案 C：将地表结构注册为 ProgressData 节点
            BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
            ProgressData progressData = beyondLevelData == null ? null : beyondLevelData.getProgressData();
            var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            for (StructureStart start : surfaceStarts) {
                List<ChunkPos> covered = boundingChunks(start.getBoundingBox());
                for (ChunkPos cp : covered) {
                    markChunk(zoneData, cp, nodeZone, level);
                }
                // 同步注册 NodeData：StructureKey = (结构 id, 中心 ChunkPos)
                if (progressData != null) {
                    BoundingBox box = start.getBoundingBox();
                    int cx = (box.minX() + box.maxX()) >> 1;
                    int cz = (box.minZ() + box.maxZ()) >> 1;
                    ChunkPos centerChunk = new ChunkPos(cx >> 4, cz >> 4);
                    Structure structure = start.getStructure();
                    ResourceLocation structureId = structureRegistry.getKey(structure);
                    if (structureId == null) {
                        continue;
                    }
                    StructureKey key = new StructureKey(structureId, centerChunk);
                    if (!progressData.getNodes().containsKey(key)) {
                        Set<Long> chunkSet = new HashSet<>();
                        for (ChunkPos cp : covered) {
                            chunkSet.add(cp.toLong());
                        }
                        NodeData nd = new NodeData(key, NodeColor.EMPTY, NodeState.LOCKED, chunkSet);
                        progressData.registerNode(nd);
                        Beyond.debugLog("[ZonePosManager] Registered node key={} chunks={}", key, chunkSet.size());
                    }
                }
            }
            // 强制同步 LevelZoneData 到客户端，使 debug 模式下的节点区域渲染可见
            level.setData(BeyondAttachInit.LEVEL_DATA, beyondLevelData);
            return;
        }

        // 未命中结构：兜底为 Pending
        addPendingPlayerZone(level, chunkPos);
    }

    @Override
    public void addPlayerActiveZone(ServerLevel level) {
        LevelZoneData zoneData = BeyondAPI.getBeyondLevelData(level).getLevelZoneData();
        ZoneType safeZone = BeyondZoneInit.SAFE_ZONE.get();
        ZoneType activeZone = BeyondZoneInit.PLAYER_ACTIVE_ZONE.get();
        ZoneType pending = BeyondZoneInit.PENDING_PLAYER_ACTIVE_ZONE.get();

        // 收集当前 ACTIVE ∪ SAFE 的 chunk 集合作为边界源
        Set<Long> edgeSet = new HashSet<>();
        for (Map.Entry<Long, ZoneType> entry : zoneData.getZonePos().entrySet()) {
            ZoneType type = entry.getValue();
            if (type == safeZone || type == activeZone) {
                edgeSet.add(entry.getKey());
            }
        }
        if (edgeSet.isEmpty()) {
            return;
        }

        // 对 4 邻域做一次扩展：邻居为 PENDING 或 EMPTY 则升级为 ACTIVE
        Set<Long> expanded = new HashSet<>();
        for (Long key : edgeSet) {
            ChunkPos self = new ChunkPos(key);
            ChunkPos[] neighbours = new ChunkPos[]{
                    new ChunkPos(self.x + 1, self.z),
                    new ChunkPos(self.x - 1, self.z),
                    new ChunkPos(self.x, self.z + 1),
                    new ChunkPos(self.x, self.z - 1)
            };
            for (ChunkPos cp : neighbours) {
                long nKey = cp.toLong();
                if (edgeSet.contains(nKey) || expanded.contains(nKey)) {
                    continue;
                }
                ZoneType cur = zoneData.getZonePos().get(nKey);
                if (cur == null || cur == BeyondZoneInit.EMPTY || cur == pending) {
                    markChunk(zoneData, cp, activeZone, level);
                    expanded.add(nKey);
                }
            }
        }

        if (!expanded.isEmpty()) {
            Beyond.debugLog("[ZonePosManager] PlayerActiveZone expanded by {} chunks.", expanded.size());
        }
    }

    @Override
    public void addPendingPlayerZone(ServerLevel level, ChunkPos chunkPos) {
        LevelZoneData zoneData = BeyondAPI.getBeyondLevelData(level).getLevelZoneData();
        ZoneType current = zoneData.getZonePos().get(chunkPos.toLong());
        if (current == null || current == BeyondZoneInit.EMPTY) {
            markChunk(zoneData, chunkPos, BeyondZoneInit.PENDING_PLAYER_ACTIVE_ZONE.get(), level);
        }
    }

    // ==================== 私有工具 ====================

    /**
     * 确保 {@link LevelZoneData#getZoneData()} 中存在对应 ZoneType 的 ZoneData 条目；
     * 若缺失则新建并调用 {@link ZoneType#initialize(List, ServerLevel)} 注入默认规则。
     */
    private static void ensureZoneData(LevelZoneData levelZoneData, ZoneType zoneType, ServerLevel level) {
        if (zoneType == null || zoneType == BeyondZoneInit.EMPTY) {
            return;
        }
        Map<ZoneType, ZoneData> map = levelZoneData.getZoneData();
        if (map.containsKey(zoneType)) {
            return;
        }
        List<RuleData> listeners = new ArrayList<>();
        zoneType.initialize(listeners, level);
        map.put(zoneType, new ZoneData(zoneType, listeners));
    }

    /**
     * 把 chunk 标记为指定 ZoneType；写入前先 {@link #ensureZoneData}。
     * PENDING 可被 SAFE / PLAYER_ACTIVE / NODE 覆盖，实现"扩展后移除 pending"。
     */
    private static void markChunk(LevelZoneData levelZoneData, ChunkPos chunkPos, ZoneType zoneType, ServerLevel level) {
        if (zoneType == null) {
            return;
        }
        ensureZoneData(levelZoneData, zoneType, level);
        levelZoneData.getZonePos().put(chunkPos.toLong(), zoneType);
    }

    /**
     * 收集该 chunk 内所有"地表结构"的 {@link StructureStart}。
     * <p>
     * TODO: 临时将所有地表结构视为节点区域，后续改为仅匹配 BeyondStructureInit 注册的节点结构表。
     */
    private static List<StructureStart> collectSurfaceStructureStarts(ServerLevel level, ChunkPos chunkPos) {
        List<StructureStart> result = new ArrayList<>();
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS, false);
        if (chunk == null) {
            return result;
        }
        int surfaceThreshold = level.getSeaLevel() - 8;
        for (StructureStart start : chunk.getAllStarts().values()) {
            if (start == null || !start.isValid()) {
                continue;
            }
            BoundingBox box = start.getBoundingBox();
            if (box.minY() >= surfaceThreshold) {
                result.add(start);
            }
        }
        return result;
    }

    /** 取结构 BoundingBox 覆盖的 ChunkPos 集合（方形最大包围区）。 */
    private static List<ChunkPos> boundingChunks(BoundingBox box) {
        List<ChunkPos> list = new ArrayList<>();
        int minChunkX = box.minX() >> 4;
        int maxChunkX = box.maxX() >> 4;
        int minChunkZ = box.minZ() >> 4;
        int maxChunkZ = box.maxZ() >> 4;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                list.add(new ChunkPos(x, z));
            }
        }
        return list;
    }
}
