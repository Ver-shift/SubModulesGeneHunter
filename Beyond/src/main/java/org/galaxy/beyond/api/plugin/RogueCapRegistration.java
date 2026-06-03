package org.galaxy.beyond.api.plugin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.init.BeyondRogueCapInit;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

/**
 * RogueCap 注册工具。
 * <p>
 * 插件只需要提交 RogueCap 构造器，具体注册表细节由 Beyond 处理。
 */
public class RogueCapRegistration {

    private static final Logger LOGGER = LogUtils.getLogger();

    public void addCap(ResourceLocation id, Supplier<? extends RogueCap> supplier) {
        if (id == null || supplier == null) return;
        BeyondRogueCapInit.registerCap(id, checkedSupplier(id, supplier));
    }

    public void addCap(ResourceLocation id, RogueCap cap) {
        if (cap == null) return;
        addCap(id, () -> cap);
    }

    public void addCap(ResourceLocation id, Class<? extends RogueCap> capClass) {
        if (id == null || capClass == null) return;
        addCap(id, () -> newInstance(capClass));
    }

    private Supplier<? extends RogueCap> checkedSupplier(ResourceLocation id, Supplier<? extends RogueCap> supplier) {
        return () -> {
            RogueCap cap = supplier.get();
            if (cap == null) {
                throw new IllegalStateException("RogueCap supplier returned null: " + id);
            }
            if (!id.equals(cap.getId())) {
                throw new IllegalStateException("RogueCap id mismatch, registry id=" + id + ", cap id=" + cap.getId());
            }
            return cap;
        };
    }

    private RogueCap newInstance(Class<? extends RogueCap> capClass) {
        try {
            Constructor<? extends RogueCap> constructor = capClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to create RogueCap: {}", capClass.getName(), e);
            throw new IllegalStateException("Failed to create RogueCap: " + capClass.getName(), e);
        }
    }
}
