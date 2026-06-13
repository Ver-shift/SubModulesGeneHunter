package org.galaxy.beyond.api.system.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局 Rogue 定义容器。
 * <p>
 * 目前主要保存已加载的关卡定义索引。实际定义内容由数据包和插件系统合并后写入。
 */
@Data
public class RogueDefinition implements IPersistedSerializable {

    @Persisted
    private final Map<ResourceLocation, ProgressDefinition> rogueProgress = new HashMap<>();

    @Persisted
    private final Map<ResourceLocation, SpawnDefinition> spawnDefinitions = new HashMap<>();

    public boolean hasProgress(ResourceLocation id) {
        return rogueProgress.containsKey(id);
    }

    public ProgressDefinition getProgress(ResourceLocation id) {
        return rogueProgress.get(id);
    }

    public boolean hasSpawnDefinition(ResourceLocation id) {
        return spawnDefinitions.containsKey(id);
    }

    public SpawnDefinition getSpawnDefinition(ResourceLocation id) {
        return spawnDefinitions.get(id);
    }

    /**
     * 校验 currentProgress 是否存在，不存在时返回错误消息。
     */
    public Component validateProgress(ResourceLocation id) {
        if (id == null) {
            return Component.translatable("beyond.definition.progress_not_set");
        }
        if (!hasProgress(id)) {
            return Component.translatable("beyond.definition.progress_not_found", id.toString());
        }
        return null;
    }
}
