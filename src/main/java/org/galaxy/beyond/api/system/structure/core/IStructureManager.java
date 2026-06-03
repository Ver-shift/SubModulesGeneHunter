package org.galaxy.beyond.api.system.structure.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IStructureManager {

    /**
     * 获取位置所在区块中所有结构占据的区块集合。
     */
    List<ChunkPos> getStructureChunks(ServerLevel level, Vec3i pos);

    /**
     * 获取位置所在区块中匹配指定 tag 的结构占据的区块集合。
     */
    List<ChunkPos> getStructureChunks(ServerLevel level, Vec3i pos, TagKey<Structure> tag);

    /**
     * 获取该位置所有结构的合并包围盒，若没有结构则返回 null。
     */
    @Nullable
    BoundingBox getStructureBoundingBox(ServerLevel level, Vec3i pos);

    /**
     * 检查该位置所在区块是否包含任意结构。
     */
    boolean hasAnyStructure(ServerLevel level, Vec3i pos);

    /**
     * 检查该位置所在区块是否包含匹配指定 tag 的结构。
     */
    boolean hasStructureByTag(ServerLevel level, Vec3i pos, TagKey<Structure> tag);
}
