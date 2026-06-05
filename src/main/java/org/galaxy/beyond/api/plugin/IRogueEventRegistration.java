package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

import java.util.function.Supplier;

/**
 * RogueEventType 注册器。
 * <p>
 * 事件 id 用于决定注册名，事件实例仍由 Supplier 延迟创建。
 */
public interface IRogueEventRegistration {

    void addEvent(Identifier id, Supplier<? extends RogueEventType> supplier);
}
