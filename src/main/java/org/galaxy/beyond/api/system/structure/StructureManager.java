package org.galaxy.beyond.api.system.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
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
    public boolean hasStructureByTag(ServerLevel level, Vec3i pos, TagKey<Structure> tag) {
        var cp = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        return !level.structureManager().startsForStructure(cp, structure ->
                registry.wrapAsHolder(structure).is(tag)
        ).isEmpty();
    }

    @Override
    public List<ChunkPos> getStructureChunks(ServerLevel level, Vec3i pos) {
        List<StructureStart> starts = getAllStarts(level, pos);
        return chunksOf(starts);
    }

    @Override
    public List<ChunkPos> getStructureChunks(ServerLevel level, Vec3i pos, TagKey<Structure> tag) {
        var cp = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        List<StructureStart> starts = level.structureManager().startsForStructure(cp, structure ->
                registry.wrapAsHolder(structure).is(tag)
        );
        return chunksOf(starts);
    }

    private static List<ChunkPos> chunksOf(List<StructureStart> starts) {
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
        return chunks.stream()
                .sorted(Comparator.comparingInt(ChunkPos::x).thenComparingInt(ChunkPos::z))
                .toList();
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
