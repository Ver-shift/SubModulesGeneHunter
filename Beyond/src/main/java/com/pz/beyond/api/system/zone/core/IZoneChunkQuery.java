package com.pz.beyond.api.system.zone.core;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

/**
 * Zone区块查询接口
 * 定义Zone对区块的基本操作方法
 */
public interface IZoneChunkQuery {

    /**
     * 添加完整区块
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     */
    void addChunk(int chunkX, int chunkZ);

    /**
     * 添加完整区块
     * @param pos 区块位置
     */
    void addChunk(ChunkPos pos);

    /**
     * 移除区块
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     */
    void removeChunk(int chunkX, int chunkZ);

    /**
     * 移除区块
     * @param pos 区块位置
     */
    void removeChunk(ChunkPos pos);

    /**
     * 检查某位置是否在此Zone内
     * @param pos 方块位置
     * @return 是否包含
     */
    boolean contains(BlockPos pos);

    /**
     * 检查某区块是否在此Zone内
     * @param pos 区块位置
     * @return 是否包含
     */
    boolean containsChunk(ChunkPos pos);

    /**
     * 检查某区块是否在此Zone内
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 是否包含
     */
    boolean containsChunk(int chunkX, int chunkZ);

    /**
     * 获取区块数量
     * @return 区块数量
     */
    int getChunkCount();

    /**
     * 是否为空（没有区块）
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 获取区块Key迭代器
     * @return 迭代器
     */
    LongIterator getChunkKeyIterator();

    /**
     * 获取所有区块Key的副本
     * @return 区块Key集合副本
     */
    LongOpenHashSet getChunkKeys();
}
