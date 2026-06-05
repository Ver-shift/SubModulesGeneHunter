package org.galaxy.beyond.api.plugin;

import org.galaxy.beyond.api.system.rogue.core.RogueCap;

import java.util.function.Supplier;

/**
 * RogueCap 注册器。
 * <p>
 * 只包装 Beyond 当前注册表，不暴露 DeferredRegister 细节。
 */
public interface IRogueCapRegistration {

    void addCap(Supplier<? extends RogueCap> supplier);
}
