package org.galaxy.beyond.api.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Beyond 插件自动发现注解。
 * <p>
 * 标记在 {@link IRoguePlugin} 实现类上后，Beyond 会在加载时自动实例化该插件。
 * 适合其他模组不直接调用 {@link BeyondPluginRunner}，只通过注解接入注册流程。
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoInit {
}
