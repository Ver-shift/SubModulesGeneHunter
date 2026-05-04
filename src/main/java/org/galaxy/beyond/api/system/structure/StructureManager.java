package org.galaxy.beyond.api.system.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;

import java.util.ArrayList;
import java.util.List;

public class StructureManager implements IStructureManager {

    /**
     * 获取指定位置的唯一结构，若存在多个或没有则返回 null。
     */
    private StructureStart getSingleStructure(ServerLevel level, Vec3i pos) {
        ChunkAccess chunkAccess = level.getChunk(new BlockPos(pos));
        List<StructureStart> starts = chunkAccess.getAllStarts().values().stream().toList();
        return starts.size() == 1 ? starts.getFirst() : null;
    }

    @Override
    public List<ChunkPos> getStructureChunks(ServerLevel level, Vec3i pos) {
        StructureStart structure = getSingleStructure(level, pos);
        if (structure == null) return List.of();

        BoundingBox box = structure.getBoundingBox();
        List<ChunkPos> chunks = new ArrayList<>();
        int minChunkX = box.minX() >> 4;
        int maxChunkX = box.maxX() >> 4;
        int minChunkZ = box.minZ() >> 4;
        int maxChunkZ = box.maxZ() >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                chunks.add(new ChunkPos(x, z));
            }
        }
        return chunks;
    }

    @Override
    public BoundingBox getStructureBoundingBox(ServerLevel level, Vec3i pos) {
        StructureStart structure = getSingleStructure(level, pos);
        return structure == null ? null : structure.getBoundingBox();
    }
}
