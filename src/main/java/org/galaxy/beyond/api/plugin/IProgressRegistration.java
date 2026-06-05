package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;

/**
 * 代码默认关卡注册器。
 * <p>
 * 这里注册的关卡会在资源包加载时注入；同 id 的数据包关卡会覆盖代码默认值。
 */
public interface IProgressRegistration {

    void addProgress(Identifier id, ProgressDefinition definition);
}
