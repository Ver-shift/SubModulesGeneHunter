package org.galaxy.beyond.api.system;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.galaxy.beyond.api.system.node.NodeManager;
import org.galaxy.beyond.api.system.rogue.RogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.structure.StructureManager;
import org.galaxy.beyond.api.system.zone.ZoneManager;

public class BeyondManager implements IBeyondManager {
    @Getter
    private final ZoneManager zoneManager = new ZoneManager();
    @Getter
    private final NodeManager nodeManager = new NodeManager();
    @Getter
    private final RogueManager rogueManager = new RogueManager();
    @Getter
    private final StructureManager structureManager = new StructureManager();



    @Override
    public void levelTick(ServerLevel level) {

    }

    @Override
    public void playerTick(ServerPlayer player) {

    }

    @Override
    public void entityTick(LivingEntity entity) {

    }

    @Override
    public void playerLogin(ServerPlayer player) {

    }

    @Override
    public void onChunkLoad(LevelChunk chunk) {
        zoneManager.onChunkLoad(chunk);
    }





}
