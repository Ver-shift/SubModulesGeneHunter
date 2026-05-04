package org.galaxy.beyond.api.system.structure.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public interface IStructureManager {

    /**
     * 根据当前的位置获取结构占据的所有区块
     */
    List<ChunkPos> getStructureChunks(ServerLevel level,Vec3i pos);

    /**
     * 获取结构边界框，若位置不存在唯一结构则返回 null。
     */
    @Nullable
    BoundingBox getStructureBoundingBox(ServerLevel level, Vec3i pos);


}

