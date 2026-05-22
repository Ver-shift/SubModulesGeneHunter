package org.galaxy.beyond.api.system;

import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.LevelChunk;
import org.galaxy.beyond.api.system.node.NodeManager;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueCapManager;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueManager;
import org.galaxy.beyond.api.system.rogue.definition.DefinitionManager;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.StructureManager;
import org.galaxy.beyond.api.system.structure.core.ISafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;
import org.galaxy.beyond.api.system.zone.ZoneManager;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public class BeyondManager implements IBeyondManager {

    private final IRogueContext rogueContext = new RogueContext();

    @Getter
    private final IZoneManager zoneManager = new ZoneManager();
    @Getter
    private final INodeManager nodeManager = new NodeManager();
    @Getter
    private final IStructureManager structureManager = new StructureManager();
    @Getter
    private final ISafeZoneStructureManager safeZoneStructureManager = new SafeZoneStructureManager();
    @Getter
    private final RogueManager rogueManager = new RogueManager(rogueContext);
    @Getter
    private final DefinitionManager definitionManager = new DefinitionManager();
    @Getter
    private final RogueCapManager rogueCapManager = new RogueCapManager(rogueContext);

    @Override
    public void onLevelLoad(ServerLevel level) {
        safeZoneStructureManager.initialize(level);
        BeyondAPI.getBeyondDimensionData(level).getRogueData().initDefaultCaps();
    }

    @Override
    public void levelTick(ServerLevel level) {
        rogueCapManager.tickZoneEvents(level);

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
        if (entity instanceof Mob mob && !entity.level().isClientSide()) {
            rogueCapManager.tickMob(mob);
        }
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
        definitionManager.onServerStarted(server);
    }
}
