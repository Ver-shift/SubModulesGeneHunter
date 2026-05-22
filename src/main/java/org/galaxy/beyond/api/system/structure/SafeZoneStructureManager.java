package org.galaxy.beyond.api.system.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.LevelData;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondMobEffectInit;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.BeyondPlayerData;
import org.galaxy.beyond.api.system.structure.core.ISafeZoneStructureManager;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;

@SuppressWarnings("deprecation")
public class SafeZoneStructureManager implements ISafeZoneStructureManager {

    private static final int FALLBACK_CHUNK_SIZE = 3;

    public void initialize(ServerLevel level) {
        if (!level.dimension().equals(BeyondAPI.getGlobalData(level).getRogueConfig().getRogueDimension())) return;

        var data = BeyondAPI.getBeyondDimensionData(level).getSafeZoneStructureData();
        if (data.getInitialized() >= 1) return;

        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        var zoneManager = BeyondAPI.getBeyondManager().getZoneManager();

        BlockPos nearest = level.findNearestMapStructure(StructureTags.VILLAGE, BlockPos.ZERO, 500, false);
        if (nearest != null) {
            BoundingBox box = structureManager.getStructureBoundingBox(level, nearest);
            if (box != null) {
                List<ChunkPos> chunks = structureManager.getStructureChunks(level, nearest);
                BlockPos safePos = findSafeSpawn(level, chunks, box);
                if (safePos != null) {
                    data.setSpawnPos(safePos);
                }
                data.setCenterPos(box.getCenter());
                data.setInitialized(1);

                BlockPos center = box.getCenter();
                int xChunks = (box.getXSpan() + 15) / 16;
                int zChunks = (box.getZSpan() + 15) / 16;
                int chunkSize = Math.max(xChunks, zChunks);
                if (chunkSize % 2 == 0) chunkSize++;
                zoneManager.addSafeZone(level, chunkSize, center);
                zoneManager.activeZoneInit(level);
                Beyond.debugInfo("Safe zone initialized via structure at {}", safePos);
                return;
            }
        }

        // 回退：以世界出生点为中心创建最小安全区
        BlockPos worldSpawn = level.getRespawnData().pos();
        BlockPos safePos = findSafeSpawnAt(level, worldSpawn);
        if (safePos != null) {
            data.setSpawnPos(safePos);
        }
        data.setCenterPos(worldSpawn);
        data.setInitialized(1);
        zoneManager.addSafeZone(level, FALLBACK_CHUNK_SIZE, worldSpawn);
        zoneManager.activeZoneInit(level);
        Beyond.debugInfo("Safe zone initialized via fallback at {}", safePos);
    }

    @Override
    public void onPlayerEnterDimension(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        if (!level.dimension().equals(BeyondAPI.getGlobalData(level).getRogueConfig().getRogueDimension())) return;

        BeyondPlayerData playerData = BeyondAPI.getBeyondPlayerData(player);
        if (playerData.getPlayerRogueData().isFirstSpawnDone()) return;

        var safeZone = BeyondAPI.getBeyondDimensionData(level).getSafeZoneStructureData();
        if (safeZone.getInitialized() < 1) return;

        BlockPos spawnPos = safeZone.getSpawnPos();
        if (!spawnPos.equals(BlockPos.ZERO)) {
            player.teleportTo(level, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    Set.of(), player.getYRot(), player.getXRot(), true);
        }

        playerData.getPlayerRogueData().setFirstSpawnDone(true);
        player.sendSystemMessage(Component.translatable("beyond.welcome"));

        if (!spawnPos.equals(BlockPos.ZERO)) {
            player.setRespawnPosition(
                    new ServerPlayer.RespawnConfig(
                            LevelData.RespawnData.of(
                                    level.dimension(), spawnPos, player.getYRot(), player.getXRot()
                            ), true
                    ), false
            );
        }
    }

    @Override
    public void trySafeZoneSpawn(ServerPlayer player) {
        var playerData = BeyondAPI.getBeyondPlayerData(player);
        if (playerData.getPlayerZoneData().isSafeZoneInitialized()) return;

        ServerLevel level = (ServerLevel) player.level();
        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;

        BlockPos spawnPos = dimData.getSafeZoneStructureData().getSpawnPos();
        if (!spawnPos.equals(BlockPos.ZERO) && isValidSpawnBlock(level, spawnPos)) {
            teleportToSafeZone(player, level, spawnPos, playerData);
            return;
        }

        var zoneEntries = dimData.getLevelZoneData().getZoneEntries();
        if (!dimData.getLevelZoneData().hasZones()) return;

        var safe = ZoneHelper.filterByMask(zoneEntries, ZoneType.Safe_Zone.mask());
        if (safe.isEmpty()) return;
        ZoneHelper.Bounds b = safe.bounds();
        BlockPos center = new BlockPos((b.minX() + b.maxX() + 1) * 8, 0, (b.minZ() + b.maxZ() + 1) * 8);
        BlockPos safePos = findSafeSpawnAt(level, center);
        if (safePos == null) return;

        teleportToSafeZone(player, level, safePos, playerData);
    }

    @Override
    public void playerTick(ServerPlayer player) {
        if (!BeyondAPI.getGlobalData(player.level()).getRogueConfig().isDebugMode()) return;

        var zone = BeyondAPI.getBeyondMobData(player).getZoneType();
        int level = switch (zone) {
            case Safe_Zone -> 1;
            case Node_Zone -> 2;
            case Active_Zone -> 3;
            default -> 0;
        };
        if (level > 0) {
            player.addEffect(new MobEffectInstance(
                    BeyondMobEffectInit.ZONE_INDICATOR, -1, level - 1,
                    false, false, false));
        }
    }

    private void teleportToSafeZone(ServerPlayer player, ServerLevel level, BlockPos pos,
                                    BeyondPlayerData playerData) {
        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot(), true);
        playerData.getPlayerZoneData().setSafeZoneInitialized(true);
    }

    private boolean isValidSpawnBlock(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinY()) return false;
        BlockState ground = level.getBlockState(pos.below());
        if (!ground.blocksMotion()) return false;
        if (!level.getBlockState(pos).isAir()) return false;
        return level.getBlockState(pos.above()).isAir();
    }

    private BlockPos findSafeSpawnAt(ServerLevel level, BlockPos center) {
        for (int r = 0; r < 32; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
                    if (top.getY() <= level.getMinY()) continue;

                    BlockState ground = level.getBlockState(top.below());
                    if (!ground.blocksMotion()) continue;

                    if (!level.getBlockState(top).isAir()) continue;
                    if (!level.getBlockState(top.above()).isAir()) continue;

                    return top;
                }
            }
        }
        return null;
    }

    private BlockPos findSafeSpawn(ServerLevel level, List<ChunkPos> chunks, BoundingBox box) {
        BlockPos center = box.getCenter();

        List<ChunkPos> sorted = chunks.stream()
                .sorted(Comparator.comparingInt(c ->
                        Math.abs(c.x() - (center.getX() >> 4)) + Math.abs(c.z() - (center.getZ() >> 4))))
                .toList();

        for (ChunkPos chunkPos : sorted) {
            int startX = (chunkPos.x() << 4) + 4;
            int startZ = (chunkPos.z() << 4) + 4;
            for (int dx = 0; dx < 8; dx++) {
                for (int dz = 0; dz < 8; dz++) {
                    int x = startX + dx;
                    int z = startZ + dz;
                    if (x < box.minX() || x > box.maxX() || z < box.minZ() || z > box.maxZ()) continue;

                    BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
                    if (isValidGround(level.getBlockState(top.below())) && isSafeStanding(level, top)) {
                        return top;
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafeStanding(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && (level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above()).blocksMotion())
                && level.canSeeSky(pos);
    }

    public static boolean isValidGround(BlockState state) {
        return state.is(Blocks.DIRT_PATH);
    }
}
