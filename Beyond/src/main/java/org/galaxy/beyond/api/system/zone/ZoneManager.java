package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.async.AsyncZoneExpansionService;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public class ZoneManager implements IZoneManager {

    private final ZoneWriter writer = new ZoneWriter();
    private final ZoneConflictResolver conflictResolver = new ZoneConflictResolver(writer);
    private final NodeColorPicker nodeColorPicker = new NodeColorPicker();
    private final SafeZoneRegistrar safeZoneRegistrar = new SafeZoneRegistrar(writer, conflictResolver);
    private final NodeZoneRegistrar nodeZoneRegistrar = new NodeZoneRegistrar(writer, conflictResolver, nodeColorPicker);
    private final AsyncZoneExpansionService asyncExpansion = new AsyncZoneExpansionService();
    private final ActiveZoneController activeZoneController = new ActiveZoneController(asyncExpansion);

    private static final TagKey<Structure> NODE_STRUCTURE_TAG =
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("beyond", "node_structure"));

    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel sl)) return;
        if (BeyondAPI.getSafeZoneStructureData(sl).getInitialized() < 1) return;
        var sm = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos wp = new BlockPos(chunk.getPos().x << 4, 0, chunk.getPos().z << 4);
        if (sm.hasStructureByTag(sl, wp, NODE_STRUCTURE_TAG)) addNodeZone(sl, wp);
    }

    @Override
    public void tick(ServerLevel level) {
        asyncExpansion.tick(level);
    }

    @Override
    public boolean addZone(ServerLevel level, ChunkPos pos, ZoneType type) {
        return writer.addZone(level, pos, type);
    }

    // ---- Safe Zone ----

    @Override
    public void addSafeZone(ServerLevel level, int chunkSize, BlockPos center) {
        safeZoneRegistrar.addSafeZone(level, chunkSize, center);
    }

    // ---- Node Zone ----

    @Override
    public void addNodeZone(ServerLevel level, BlockPos pos) {
        nodeZoneRegistrar.addNodeZone(level, pos);
    }

    // ---- Active Zone ----

    @Override
    public void activeZoneInit(ServerLevel level) {
        activeZoneController.activeZoneInit(level);
    }

    @Override
    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        activeZoneController.addActiveZone(level, nodeData);
    }
}
