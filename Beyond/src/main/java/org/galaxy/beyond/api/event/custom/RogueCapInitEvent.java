package org.galaxy.beyond.api.event.custom;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.rogue.RogueData;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * RogueCap 初始化事件族。
 * <p>
 * Beyond 初始化 {@link RogueData} 时会先发布 {@link Default}，用于决定所有关卡都会默认装载的 Cap；
 * 随后在当前关卡存在时发布 {@link Progress}，用于追加只在该关卡生效的 Cap。
 * <p>
 * 监听者可以直接在 {@link Default} 或 {@link Progress} 上调用 {@link #addCap(ResourceLocation)} 和
 * {@link #removeCap(ResourceLocation)}。每一次具体增删都会继续发布 {@link Add} 或 {@link Remove}，
 * 其他模组可以取消这次增删，或者记录来源。
 * <p>
 * 这里操作的是 RogueCap id，不注册 Cap 类型。所有 id 必须已经存在于 Beyond RogueCap 注册表，
 * 否则后续初始化会抛出错误以暴露配置问题。
 */
public abstract class RogueCapInitEvent extends Event {

    private final RogueData rogueData;
    private final Set<ResourceLocation> capIds = new LinkedHashSet<>();

    protected RogueCapInitEvent(RogueData rogueData, List<ResourceLocation> capIds) {
        this.rogueData = rogueData;
        if (capIds != null) {
            capIds.stream().filter(id -> id != null).forEach(this.capIds::add);
        }
    }

    public RogueData getRogueData() {
        return rogueData;
    }

    public List<ResourceLocation> getCapIds() {
        return List.copyOf(capIds);
    }

    public void addCap(ResourceLocation id) {
        if (id == null) return;
        Add event = post(new Add(this, id));
        if (!event.isCanceled()) {
            capIds.add(id);
        }
    }

    public void removeCap(ResourceLocation id) {
        if (id == null) return;
        Remove event = post(new Remove(this, id));
        if (!event.isCanceled()) {
            capIds.remove(id);
        }
    }

    public static <T extends Event> T post(T event) {
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    /**
     * 全局默认 RogueCap 初始化事件。
     * <p>
     * 触发时机：新的 {@link RogueData} 首次创建 Cap 列表时。这里适合加入所有关卡都需要的能力，
     * 例如进度启动、玩家状态、节点状态等基础 Cap。
     */
    public static class Default extends RogueCapInitEvent {
        public Default(RogueData rogueData, List<ResourceLocation> capIds) {
            super(rogueData, capIds);
        }
    }

    /**
     * 当前关卡专属 RogueCap 初始化事件。
     * <p>
     * 触发时机：全局默认 Cap 初始化完成后，并且当前 RogueData 拥有有效 progress id 时。
     * {@code progressDefinition} 是该关卡定义，监听者可以根据定义内容追加或移除关卡专属 Cap。
     */
    public static class Progress extends RogueCapInitEvent {
        private final ResourceLocation progressId;
        private final ProgressDefinition progressDefinition;

        public Progress(RogueData rogueData, ResourceLocation progressId, ProgressDefinition progressDefinition, List<ResourceLocation> capIds) {
            super(rogueData, capIds);
            this.progressId = progressId;
            this.progressDefinition = progressDefinition;
        }

        public ResourceLocation getProgressId() {
            return progressId;
        }

        public ProgressDefinition getProgressDefinition() {
            return progressDefinition;
        }
    }

    /**
     * 单次添加 Cap 事件。
     * <p>
     * 由 {@link #addCap(ResourceLocation)} 自动发布。取消该事件会阻止这次添加。
     */
    public static class Add extends Event implements ICancellableEvent {
        private final RogueCapInitEvent owner;
        private final ResourceLocation capId;

        public Add(RogueCapInitEvent owner, ResourceLocation capId) {
            this.owner = owner;
            this.capId = capId;
        }

        public RogueCapInitEvent getOwner() {
            return owner;
        }

        public ResourceLocation getCapId() {
            return capId;
        }
    }

    /**
     * 单次移除 Cap 事件。
     * <p>
     * 由 {@link #removeCap(ResourceLocation)} 自动发布。取消该事件会阻止这次移除。
     */
    public static class Remove extends Event implements ICancellableEvent {
        private final RogueCapInitEvent owner;
        private final ResourceLocation capId;

        public Remove(RogueCapInitEvent owner, ResourceLocation capId) {
            this.owner = owner;
            this.capId = capId;
        }

        public RogueCapInitEvent getOwner() {
            return owner;
        }

        public ResourceLocation getCapId() {
            return capId;
        }
    }
}
