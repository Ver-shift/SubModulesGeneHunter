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
    //将eventEntry转化为eventTask。同时。发布neoforge event。
    EventTask resolveEvent(ServerLevel level, EncounterType encounterType);

    List<SceneType> resolveScenes(ServerLevel level);

    
}
