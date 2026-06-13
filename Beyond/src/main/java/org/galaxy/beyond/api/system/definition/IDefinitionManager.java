package org.galaxy.beyond.api.system.definition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.spawn.character.Character;

import java.util.List;

/**
 * Definition 解析服务。
 * <p>
 * 负责把数据包和插件提供的定义数据解析为运行时数据，同时发布 ResolveEvent，允许其他模组在解析结果进入运行态前插入或替换内容。
 */
public interface IDefinitionManager {

    /**
     * 服务器启动完成时应用等待中的数据包定义。
     */
    void onServerStarted(MinecraftServer server);

    /**
     * 按遭遇类型解析当前关卡的事件任务。
     */
    EventTask resolveEvent(ServerLevel level, EncounterType encounterType);

    /**
     * 解析当前关卡的场景序列。
     */
    List<SceneType> resolveScenes(ServerLevel level);

    /**
     * 解析当前关卡使用的刷怪定义 id。
     */
    ResourceLocation resolveSpawnDefinition(ServerLevel level);

    /**
     * 解析当前关卡专属 RogueCap id。
     */
    List<ResourceLocation> resolveProgressCaps(ServerLevel level);

    /**
     * 解析刷怪定义使用的 Character。
     */
    Character getSpawnCharacter(SpawnDefinition definition);
}
