package com.pz.beyond.api.system.zone.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface IZonePosManager {

    /**
     * 增加或者减小安全区，安全区为方形区域。
     * 以 {@code StructureData.spawnPos}（村庄位置）所在区块为中心铺设。
     *
     * @param level          服务端维度
     * @param chunkCountSize 方形边长（区块数），奇数更佳
     */
    void addSafeZone(ServerLevel level, int chunkCountSize);

    /**
     * 在区块加载/生成的时候，根据算法决定该区块归属的区域类型。
     * 若该区块内存在地表结构则标记为节点区域；否则回落为待活动区域。
     *
     * @param level    服务端维度
     * @param chunkPos 区块坐标
     */
    void spawnZone(ServerLevel level, ChunkPos chunkPos);

    /**
     * 将当前活动区边界向外推进一层：把邻接的待活动区块转换为玩家可活动区。
     *
     * @param level 服务端维度
     */
    void addPlayerActiveZone(ServerLevel level);

    /**
     * 把指定区块兜底标记为玩家待活动区域（仅当该区块尚未归属任何区域时）。
     *
     * @param level    服务端维度
     * @param chunkPos 区块坐标
     */
    void addPendingPlayerZone(ServerLevel level, ChunkPos chunkPos);

    //生成区块的时候，生成玩家待活动区域，如果是有结构，就生成节点区域，节点区域是获取结构的最大区域的一个方形。

    //拓展活动区的时候，先进行拓展，然后将拓展的区域的待活动区域移除。


}
