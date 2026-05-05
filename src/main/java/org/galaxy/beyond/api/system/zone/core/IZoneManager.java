package org.galaxy.beyond.api.system.zone.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.List;

public interface IZoneManager {

    void onChunkLoad(ChunkAccess chunk);

    void addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType);

    /**
     * 安全区控制，通过寻找结构进行初始化
     */
    void safeZoneInit(ServerLevel serverLevel);

    /**
     * 添加安全区区块
     * @param chunkSize 区块的长或者宽，以正方形扩宽，
     * @param center   安全区中心
     */
    void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center);
    /**
     * 每次结构区域加载的时候进行创建
     * @param pos 结构内的世界坐标，用于定位唯一结构
     */
    void addNodeZone(ServerLevel serverLevel, BlockPos pos);

    /**
     * 玩家可活动区域,通过玩家节点活动进行自动扩容。记得想办法保持玩家的最小加载区域
     */
    void activeZoneInit(ServerLevel serverLevel);
    void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData);

    //增删查改区域的cap 单个区域只会有一个cap能够生效。
    void addCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType);
    void removeCap(ServerLevel serverLevel,ZoneType type, ZoneCapType zoneCapType);
    void clearCap(ServerLevel serverLevel,ZoneType type);
    List<ZoneCapType> getCaps(ServerLevel serverLevel,ZoneType type);

    void setCapLevel(ServerLevel serverLevel,ZoneType type, ZoneCapType zoneCapType,int capLevel);
    void addCapLevel(ServerLevel serverLevel,ZoneType type, ZoneCapType zoneCapType,int capLevel);

    /**
     * 统一入口：遍历玩家，检测区域变化，触发 levelTick/playerTick/changeZone，
     * 并发布 {@link org.galaxy.beyond.api.event.custom.PlayerChangeZoneEvent} 到 EVENT_BUS。
     */
    void handleZoneRule(ServerLevel level);

    void handlePlayerRightClickBlock(ServerPlayer player, BlockPos pos);

    void handleMobTick(Mob mob);
}
