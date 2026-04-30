package com.pz.beyond.api.system.structure;

import com.mojang.datafixers.util.Pair;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.util.TeleportUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;

/**
 * 结构管理器 — 在向日葵平原中查找普通村庄并设置安全出生点。
 */
public class StructureManager implements IStructureManager{

    private static final int BIOME_SEARCH_RADIUS = 6400;
    private static final int STRUCTURE_SEARCH_RADIUS = 256;
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 世界加载时调用，只执行一次。
     * 策略：向日葵平原村庄 → 任意普通村庄 → 默认出生点。
     */
    public void findSafeZone(ServerLevel level) {
        StructureData data = BeyondAPI.getBeyondLevelData(level).getStructureData();
        if (data.hasValidSpawnPos()) {
            Beyond.debugLog("Spawn already set: {}", data.getSpawnPos());
            return;
        }

        BlockPos village = findSunflowerPlainsVillage(level);
        if (village == null) {
            Beyond.debugLog("No sunflower plains village, trying any village...");
            village = findAnyNormalVillage(level);
        }

        BlockPos spawn = (village != null)
                ? TeleportUtil.findSafeTeleportPos(level, village)
                : TeleportUtil.findSafeTeleportPos(level, level.getSharedSpawnPos());

        data.setSpawnPos(spawn);
        level.setDefaultSpawnPos(spawn, 0.0f);
        Beyond.debugLog("World spawn set to: {}", spawn);
    }

    // ==================== 向日葵平原村庄搜索 ====================

    /**
     * 在向日葵平原中查找普通（非僵尸）村庄。
     * 找到后验证群系，不符合则跳过继续搜索。
     */
    private BlockPos findSunflowerPlainsVillage(ServerLevel level) {
        // 1. 先找到向日葵平原群系
        var biomeResult = level.findClosestBiome3d(
                b -> b.is(Biomes.SUNFLOWER_PLAINS),
                new BlockPos(0, 64, 0),
                BIOME_SEARCH_RADIUS / 16, 32, 64
        );
        if (biomeResult == null) return null;

        // 2. 构建只匹配 village_plains 的 HolderSet
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var holder = registry.getHolder(BuiltinStructures.VILLAGE_PLAINS);
        if (holder.isEmpty()) return null;
        HolderSet<Structure> set = HolderSet.direct(holder.get());

        // 3. 从群系位置开始，多次尝试查找合适村庄
        BlockPos search = biomeResult.getFirst();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, set, search, STRUCTURE_SEARCH_RADIUS, false);
            if (result == null) break;

            BlockPos pos = result.getFirst();

            // 验证：必须在向日葵平原 + 非僵尸村庄
            if (level.getBiome(pos).is(Biomes.SUNFLOWER_PLAINS) && !isZombieVillage(level, pos)) {
                return pos;
            }

            Beyond.debugLog("Village at {} rejected (attempt {}), continuing...", pos, i + 1);
            search = pos.offset(64, 0, 64);
        }
        return null;
    }

    /**
     * 回退策略：查找任意普通（非僵尸）村庄。
     */
    private BlockPos findAnyNormalVillage(ServerLevel level) {
        BlockPos search = level.getSharedSpawnPos();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            BlockPos pos = level.findNearestMapStructure(
                    net.minecraft.tags.StructureTags.VILLAGE, search, STRUCTURE_SEARCH_RADIUS * 2, false);
            if (pos == null) break;
            if (!isZombieVillage(level, pos)) return pos;
            search = pos.offset(64, 0, 64);
        }
        return null;
    }

    // ==================== 僵尸村庄检测 ====================

    /**
     * 通过检查结构片段模板是否包含 "zombie" 来判断。
     */
    private boolean isZombieVillage(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(
                new ChunkPos(pos).x, new ChunkPos(pos).z, ChunkStatus.STRUCTURE_STARTS, false);
        if (chunk == null) return false;

        for (StructureStart start : chunk.getAllStarts().values()) {
            if (start == null || !start.isValid()) continue;
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof PoolElementStructurePiece pool
                        && pool.getElement().toString().toLowerCase().contains("zombie")) {
                    return true;
                }
            }
        }
        return false;
    }
}
