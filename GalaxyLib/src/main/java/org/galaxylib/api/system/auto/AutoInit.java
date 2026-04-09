package org.galaxylib.api.system.auto;

/**
 * 通过注解和
 */
public @interface AutoInit {

    String initType();
    String modName();

    int priority() default 0;


    /**
     * 方便查找已经支持了的注册支持
     */
    //todo: 完善自动注册
    enum InitType {
        /** 基因类型 */
        GENE("gene"),
        /** 词条类型 */
        TRAIT("trait");


        InitType(final String name) {
            this.name = name.toLowerCase();
        }
        private String name;


    }
}
