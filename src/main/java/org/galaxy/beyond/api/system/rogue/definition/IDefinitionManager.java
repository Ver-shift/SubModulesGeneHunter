package org.galaxy.beyond.api.system.rogue.definition;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.List;

/**
 * 进行一些数据转换，主要功能是将定义数据转移到运行数据那边去。
 */
public interface IDefinitionManager {

    /** 服务器启动时加载待处理的数据包 */
    void onServerStarted(MinecraftServer server);

    /** 将 EncounterType 解析为 EventTask，发布 NeoForge 事件 */
    EventTask resolveEvent(ServerLevel level, EncounterType encounterType);

    /** 将 ProgressType 解析为 SceneType 列表 */
    List<SceneType> resolveScenes(ServerLevel level);
}
