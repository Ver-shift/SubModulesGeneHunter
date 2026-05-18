package org.galaxy.beyond.api;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.galaxy.beyond.api.pack.ProgressDataPack;
import org.galaxy.beyond.api.system.node.NodeManager;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.rogue.RogueManager;
import org.galaxy.beyond.api.system.rogue.definition.DefinitionManager;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.StructureManager;
import org.galaxy.beyond.api.system.structure.core.ISafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;
import org.galaxy.beyond.api.system.zone.ZoneManager;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public class BeyondManager implements IBeyondManager {
    @Getter
    private final IZoneManager zoneManager = new ZoneManager();
    @Getter
    private final INodeManager nodeManager = new NodeManager();
    @Getter
    private final IStructureManager structureManager = new StructureManager();
    @Getter
    private final ISafeZoneStructureManager safeZoneStructureManager = new SafeZoneStructureManager();
    @Getter
    private final RogueManager rogueManager = new RogueManager();
    @Getter
    private final DefinitionManager definitionManager = new DefinitionManager();

    @Override
    public void onLevelLoad(ServerLevel level) {
        safeZoneStructureManager.initialize(level);
    }

    private boolean serverReady;

    @Override
    public void levelTick(ServerLevel level) {
        zoneManager.handleZoneRule(level);
        if (!serverReady) return;
        if (level.dimension().equals(BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().getRogueDimension())) {
            rogueManager.tick(level);
        }
    }

    @Override
    public void playerTick(ServerPlayer player) {
        safeZoneStructureManager.playerTick(player);
    }

    @Override
    public void entityTick(LivingEntity entity) {
    }

    @Override
    public void playerChangedDimension(ServerPlayer player) {
        safeZoneStructureManager.onPlayerEnterDimension(player);
        safeZoneStructureManager.trySafeZoneSpawn(player);
    }

    @Override
    public void playerLoggedIn(ServerPlayer player) {
        safeZoneStructureManager.onPlayerEnterDimension(player);
        safeZoneStructureManager.trySafeZoneSpawn(player);
    }

    @Override
    public void onChunkLoad(LevelChunk chunk) {
        zoneManager.onChunkLoad(chunk);
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        var globalData = BeyondAPI.getGlobalData(server.overworld());
        if (globalData != null) {
            globalData.getRogueConfig().reset();
        }
        ProgressDataPack.applyPending(server);
        serverReady = true;
    }

    @Override
    public void onPlayerRightClickBlock(ServerPlayer player, BlockPos pos) {
        zoneManager.handlePlayerRightClickBlock(player, pos);
    }
}
