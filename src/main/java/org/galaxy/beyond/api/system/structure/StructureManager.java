package org.galaxy.beyond.api.system.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.*;

public class StructureManager implements IStructureManager {

    private List<StructureStart> getAllStarts(ServerLevel level, Vec3i pos) {
        ChunkAccess chunk = level.getChunk(new BlockPos(pos));
        return new ArrayList<>(chunk.getAllStarts().values());
    }

    @Override
    public boolean hasAnyStructure(ServerLevel level, Vec3i pos) {
        return !getAllStarts(level, pos).isEmpty();
    }

    @Override
    public List<ChunkPos> getStructureChunks(ServerLevel level, Vec3i pos) {
        List<StructureStart> starts = getAllStarts(level, pos);
        if (starts.isEmpty()) return List.of();

        Set<ChunkPos> chunks = new HashSet<>();
        for (StructureStart start : starts) {
            BoundingBox box = start.getBoundingBox();
            int minCX = box.minX() >> 4;
            int maxCX = box.maxX() >> 4;
            int minCZ = box.minZ() >> 4;
            int maxCZ = box.maxZ() >> 4;
            chunks.addAll(new ZoneHelper.Bounds(minCX, minCZ, maxCX, maxCZ).allChunks());
        }
        return new ArrayList<>(chunks);
    }

    @Override
    public BoundingBox getStructureBoundingBox(ServerLevel level, Vec3i pos) {
        List<StructureStart> starts = getAllStarts(level, pos);
        if (starts.isEmpty()) return null;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (StructureStart start : starts) {
            BoundingBox box = start.getBoundingBox();
            if (box.minX() < minX) minX = box.minX();
            if (box.minY() < minY) minY = box.minY();
            if (box.minZ() < minZ) minZ = box.minZ();
            if (box.maxX() > maxX) maxX = box.maxX();
            if (box.maxY() > maxY) maxY = box.maxY();
            if (box.maxZ() > maxZ) maxZ = box.maxZ();
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
