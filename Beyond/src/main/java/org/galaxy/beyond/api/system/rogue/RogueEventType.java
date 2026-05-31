package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondEventInit;

/**
 * 肉鸽事件类型 —— <b>核心事件执行逻辑的抽象基类</b>。
 * <p>
 * 每种事件（怪物、Boss、商店、治疗、奖励等）通过继承此类定义自己的行为。
 * 具体子类注册到 {@link org.galaxy.beyond.api.init.BeyondEventInit}，由
 * {@link org.galaxy.beyond.api.system.rogue.core.RogueEncounterRunner} 按顺序调用。
 * <p>
 * <b>生命周期：</b>
 * <ol>
 *   <li>{@link #cast(Context)} — 事件开始时调用，执行初始化逻辑（刷怪、开店等）</li>
 *   <li>{@link #next(Context)} — 逐帧 / 玩家交互时调用，检查事件是否完成</li>
 * </ol>
 * <p>
 * <b>返回值约定：</b>
 * <ul>
 *   <li>{@link Result#SUCCESS} — 事件完成，允许推进到下一事件</li>
 *   <li>{@link Result#FAILURE} — 事件未完成 / 条件不满足，阻止推进</li>
 *   <li>{@link Result#EMPTY} — 仅作兜底 / 未初始化时的默认值，不要在正常逻辑中使用</li>
 * </ul>
 * <p>
 * <b>与 {@link org.galaxy.beyond.api.event.custom.RogueEncounterEvent} 的关系：</b><br>
 * RogueEventType 是<u>内部核心</u>，定义事件"做什么"；<br>
 * RogueEncounterEvent 是<u>外部扩展点</u>，供其他模组在事件前后挂载逻辑。
 * <p>
 * <b>线程安全：</b>单例（Registry 缓存同一实例），cast/next 在服务端 tick 线程串行调用，实例字段可直接读写。
 */
public abstract class RogueEventType implements IPersistedSerializable {
    private final ResourceLocation id;

    public RogueEventType(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getDisplayName() {
        return Component.translatable(id.getNamespace() + ".event." + id.getPath());
    }

    public abstract void cast(Context context);

    @NonNull
    public abstract Result next(Context context);

    /**
     * Rogue 事件运行上下文。
     *
     * @param type         当前遭遇类型
     * @param level        事件所在服务端世界
     * @param nodePos      玩家点击的节点方块位置；双高节点统一传入下半部分位置
     * @param rogueContext 当前 Rogue 上下文，供事件读取玩家、阶段和全局数据
     */
    public record Context(EncounterType type, ServerLevel level, BlockPos nodePos, IRogueContext rogueContext) {
    }

    public enum Result {
        SUCCESS, FAILURE, EMPTY;

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }

    private static RogueEventType resolve(ResourceLocation id) {
        return BeyondEventInit.get(id);
    }

    public static final MapCodec<RogueEventType> CODEC =
            ResourceLocation.CODEC.xmap(RogueEventType::resolve, RogueEventType::getId).fieldOf("id");

    public static final StreamCodec<ByteBuf, RogueEventType> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(RogueEventType::resolve, RogueEventType::getId);
}
