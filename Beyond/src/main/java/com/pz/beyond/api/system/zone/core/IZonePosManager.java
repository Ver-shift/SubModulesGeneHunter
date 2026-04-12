package com.pz.beyond.api.system.zone.core;

import net.minecraft.world.level.ChunkPos;

public interface IZonePosManager {




    /**
     * 增加或者减小安全区，安全区为方形区域
     *
     * @param chunkCountSize
     */
    void addSafeZone(int chunkCountSize);

    /**
     * 在区块生成的时候，根据算法生成不同的节点区域.
     * @param chunkPos
     */
    void spawnZone(ChunkPos chunkPos);

    /**
     * 增加或者减少玩家可活动范围，根据条件拓展。
     */
    void addPlayerActiveZone();

    /**
     * 所有区块加载的时候，都是先生成待活动区域，
     */
    void addPendingPlayerZone();

    //生成区块的时候，生成玩家待活动区域，如果是有结构，就生成节点区域，节点区域是获取结构的最大区域的一个方形。

    //拓展活动区的时候，先进行拓展，然后将拓展的区域的待活动区域移除。


}
