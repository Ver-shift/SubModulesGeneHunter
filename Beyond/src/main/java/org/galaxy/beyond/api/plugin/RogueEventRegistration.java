package org.galaxy.beyond.api.plugin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.init.BeyondEventInit;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

/**
 * RogueEventType 注册工具。
 * <p>
 * id 用于决定注册名，Supplier 用于延迟创建事件实例。
 */
public class RogueEventRegistration {

    private static final Logger LOGGER = LogUtils.getLogger();

    public void addEvent(ResourceLocation id, Supplier<? extends RogueEventType> supplier) {
        if (id == null || supplier == null) return;
        BeyondEventInit.register(id, checkedSupplier(id, supplier)::get);
    }

    public void addEvent(ResourceLocation id, RogueEventType event) {
        if (event == null) return;
        addEvent(id, () -> event);
    }

    public void addEvent(ResourceLocation id, Class<? extends RogueEventType> eventClass) {
        if (id == null || eventClass == null) return;
        addEvent(id, () -> newInstance(eventClass));
    }

    private Supplier<? extends RogueEventType> checkedSupplier(ResourceLocation id, Supplier<? extends RogueEventType> supplier) {
        return () -> {
            RogueEventType event = supplier.get();
            if (event == null) {
                throw new IllegalStateException("RogueEventType supplier returned null: " + id);
            }
            if (!id.equals(event.getId())) {
                throw new IllegalStateException("RogueEventType id mismatch, registry id=" + id + ", event id=" + event.getId());
            }
            return event;
        };
    }

    private RogueEventType newInstance(Class<? extends RogueEventType> eventClass) {
        try {
            Constructor<? extends RogueEventType> constructor = eventClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to create RogueEventType: {}", eventClass.getName(), e);
            throw new IllegalStateException("Failed to create RogueEventType: " + eventClass.getName(), e);
        }
    }
}
