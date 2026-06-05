package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.async.AsyncZoneExpansionService;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.concurrent.CompletableFuture;

public class ZoneManager implements IZoneManager {

    private final ZoneWriter writer = new ZoneWriter();
    private final ZoneConflictResolver conflictResolver = new ZoneConflictResolver(writer);
    private final NodeColorPicker nodeColorPicker = new NodeColorPicker();
    private final SafeZoneRegistrar safeZoneRegistrar = new SafeZoneRegistrar(writer, conflictResolver);
    private final NodeZoneRegistrar nodeZoneRegistrar = new NodeZoneRegistrar(writer, conflictResolver, nodeColorPicker);
    private final AsyncZoneExpansionService asyncExpansion = new AsyncZoneExpansionService();
    private final ActiveZoneController activeZoneController = new ActiveZoneController(asyncExpansion);

    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel sl)) return;
        var rogueConfig = CommonConfig.getRogueDimensionConfig(sl);
        if (rogueConfig.isEmpty()) return;
        if (BeyondAPI.getSafeZoneStructureData(sl).getInitialized() < 1) return;
        var sm = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos wp = new BlockPos(chunk.getPos().x() << 4, 0, chunk.getPos().z() << 4);
        TagKey<Structure> nodeStructureTag = TagKey.create(Registries.STRUCTURE, rogueConfig.get().nodeStructureTag());
        if (sm.hasStructureByTag(sl, wp, nodeStructureTag)) addNodeZone(sl, wp);
    }

    @Override
    public void tick(ServerLevel level) {
        if (!CommonConfig.isRogueDimension(level)) return;

        asyncExpansion.tick(level);
    }

    @Override
    public boolean addZone(ServerLevel level, ChunkPos pos, ZoneType type) {
        if (!CommonConfig.isRogueDimension(level)) return false;

        return writer.addZone(level, pos, type);
    }

    // ---- Safe Zone ----

    @Override
    public void addSafeZone(ServerLevel level, int chunkSize, BlockPos center) {
        if (!CommonConfig.isRogueDimension(level)) return;

        safeZoneRegistrar.addSafeZone(level, chunkSize, center);
    }

    // ---- Node Zone ----

    @Override
    public void addNodeZone(ServerLevel level, BlockPos pos) {
        if (!CommonConfig.isRogueDimension(level)) return;

        nodeZoneRegistrar.addNodeZone(level, pos);
    }

    @Override
    public CompletableFuture<NodeData> discoverNearestNode(ServerLevel level, ChunkPos center, int radius) {
        if (!CommonConfig.isRogueDimension(level)) return CompletableFuture.completedFuture(null);

        var rogueConfig = CommonConfig.getRogueDimensionConfig(level);
        if (rogueConfig.isEmpty()) return CompletableFuture.completedFuture(null);

        BlockPos searchPos = new BlockPos(center.x() << 4, 0, center.z() << 4);
        TagKey<Structure> nodeStructureTag = TagKey.create(Registries.STRUCTURE, rogueConfig.get().nodeStructureTag());
        BlockPos nearest = level.findNearestMapStructure(nodeStructureTag, searchPos, radius, false);
        if (nearest == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.completedFuture(nodeZoneRegistrar.addNodeZone(level, nearest));
    }

    // ---- Active Zone ----

    @Override
    public void activeZoneInit(ServerLevel level) {
        if (!CommonConfig.isRogueDimension(level)) return;

        activeZoneController.activeZoneInit(level);
    }

    @Override
    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        if (!CommonConfig.isRogueDimension(level)) return;

        activeZoneController.addActiveZone(level, nodeData);
    }

    public boolean expandFromWorldSeed(ServerLevel level, ChunkPos pos) {
        if (!CommonConfig.isRogueDimension(level)) return false;

        return activeZoneController.expandFromWorldSeed(level, pos);
    }

    @Override
    public void sendActiveZoneProgress(ServerPlayer player) {
        if (!CommonConfig.isRogueDimension(player.level())) return;

        asyncExpansion.sendActiveZoneProgress(player);
    }
}
