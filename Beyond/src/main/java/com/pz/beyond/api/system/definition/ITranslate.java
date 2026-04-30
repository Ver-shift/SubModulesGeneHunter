package com.pz.beyond.api.system.definition;

/**
 * 将静态的 <b>definition</b> 定义数据转化为运行时数据的通用接口。
 * <p>
 * 通过数据包 / 配置加载的 definition 类应实现此接口，将自身携带的配置
 * （草图 / 权重 / 范围等）结合输入上下文（随机源 / 玩家 / 世界等） roll 出具体的运行时实例。
 * </p>
 *
 * @param <T> 转化后的运行时数据类型
 * @param <V> 转化所需的输入上下文（通常为 {@link net.minecraft.world.level.levelgen.SingleThreadedRandomSource}）
 */
public interface ITranslate<T, V> {

    /**
     * 将当前 definition 转化为运行时数据。
     *
     * @param input 转化上下文（如随机源）
     * @return 转化后的运行时实例
     */
    T translate(V input);
}
