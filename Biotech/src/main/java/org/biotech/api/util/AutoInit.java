package org.biotech.api.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动注册注解 - 标记需要自动注册的类
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoInit {

    /**
     * 注册类型
     */
    InitType type();

    /**
     * 注册类型枚举
     */
    enum InitType {
        /** 基因类型 */
        GENE,
        /** 词条类型 */
        TRAIT,
        /** 异种类型 */
        XENE
    }
}
