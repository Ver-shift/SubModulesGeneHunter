package org.galaxy.beyond.api.system;

import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.LevelChunk;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.node.NodeManager;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueCapManager;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueManager;
import org.galaxy.beyond.api.system.definition.DefinitionManager;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.StructureManager;
import org.galaxy.beyond.api.system.structure.core.ISafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;
import org.galaxy.beyond.api.system.zone.ZoneManager;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.Map;
import java.util.WeakHashMap;

public class BeyondManager implements IBeyondManager {

    @Getter
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
    /** Last allowed chunk per player, used to reject crossing the active-zone edge. */
    private final Map<ServerPlayer, net.minecraft.world.level.ChunkPos> lastAllowedChunks = new WeakHashMap<>();

    @Override
    public void onLevelLoad(ServerLevel level) {
        if (!CommonConfig.isRogueDimension(level)) return;

        safeZoneStructureManager.initialize(level);
        BeyondAPI.getRogueData(level).initDefaultCaps(level);
    }

    @Override
    public void levelTick(ServerLevel level) {
        if (!CommonConfig.isRogueDimension(level)) return;

        zoneManager.tick(level);
        rogueCapManager.tickZoneEvents(level);
        rogueManager.tick(level);
    }

    @Override
    public void playerTick(ServerPlayer player) {
        safeZoneStructureManager.playerTick(player);
        enforceZoneBoundary(player);
    }

    /**
     * Server-side movement guard for the active-zone boundary. The zone is chunk-granular, just
     * like the existing border renderer: safe, node and active chunks are traversable, all other
     * chunks are outside. Clamping only on a transition keeps this cheap for normal movement.
     */
    private void enforceZoneBoundary(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || !CommonConfig.isRogueDimension(level)) {
            lastAllowedChunks.remove(player);
            return;
        }

        net.minecraft.world.level.ChunkPos current = player.chunkPosition();
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data.hasAny(current)) {
            lastAllowedChunks.put(player, current);
            return;
        }

        net.minecraft.world.level.ChunkPos previous = lastAllowedChunks.get(player);
        if (previous == null) return;

        double x = player.getX();
        double z = player.getZ();
        double epsilon = 0.05D;
        if (current.x > previous.x) x = previous.getMaxBlockX() + 1.0D - epsilon;
        else if (current.x < previous.x) x = previous.getMinBlockX() + epsilon;
        if (current.z > previous.z) z = previous.getMaxBlockZ() + 1.0D - epsilon;
        else if (current.z < previous.z) z = previous.getMinBlockZ() + epsilon;

        player.setPos(x, player.getY(), z);
        var velocity = player.getDeltaMovement();
        double vx = current.x == previous.x ? velocity.x : 0.0D;
        double vz = current.z == previous.z ? velocity.z : 0.0D;
        player.setDeltaMovement(vx, velocity.y, vz);
    }

    @Override
    public void entityTick(LivingEntity entity) {
        if (entity instanceof Mob mob && !entity.level().isClientSide()
                && CommonConfig.isRogueDimension(entity.level())) {
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
        definitionManager.onServerStarted(server);
    }
}
