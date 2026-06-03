package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.cap.NodeCap;
import org.galaxy.beyond.api.system.rogue.cap.NodeZoneEnterCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerInGameCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerProgressFinishCap;
import org.galaxy.beyond.api.system.rogue.cap.ProgressStartCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueInitCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueProgressFinishCap;
import org.galaxy.beyond.data.progress.ForestProgress;
import org.galaxy.beyond.data.progress.TutorialProgress;
import org.galaxy.beyond.rogue_event.BossEventType;
import org.galaxy.beyond.rogue_event.HealEventType;
import org.galaxy.beyond.rogue_event.MonsterEventType;
import org.galaxy.beyond.rogue_event.RewardEventType;
import org.galaxy.beyond.rogue_event.ShopEventType;

/**
 * Beyond 插件写法示例。
 * <p>
 * 这个类故意不添加 {@link AutoInit}，因此不会被 Beyond 自动扫描运行。
 * 如果要让插件实际生效，可以给自己的插件类添加 {@code @AutoInit}，
 * 或者手动调用 {@link BeyondPluginRunner#addPlugin(IRoguePlugin)}。
 */
public class BeyondPluginExample implements IRoguePlugin {

    @Override
    public ResourceLocation getId() {
        return Beyond.asResource("plugin_example");
    }

    /**
     * 注册当前 Beyond 支持的所有 RogueCap 类型。
     * <p>
     * 注意：这些内置 Cap 已经由 {@code BeyondRogueCapInit} 注册。
     * 本方法只是示例，不应在当前内置插件里实际调用，否则会重复注册同 id。
     */
    @Override
    public void registerRogueCaps(RogueCapRegistration registration) {
        registration.addCap(ProgressStartCap.ID, ProgressStartCap::new);
        registration.addCap(PlayerInGameCap.ID, PlayerInGameCap.class);
        registration.addCap(NodeCap.ID, new NodeCap());
        registration.addCap(NodeZoneEnterCap.ID, NodeZoneEnterCap::new);
        registration.addCap(RogueInitCap.ID, RogueInitCap.class);
        registration.addCap(RogueProgressFinishCap.ID, new RogueProgressFinishCap());
        registration.addCap(PlayerProgressFinishCap.ID, PlayerProgressFinishCap::new);
    }

    /**
     * 注册当前 Beyond 支持的所有 RogueEventType 类型。
     * <p>
     * id 通常直接使用事件类自己的 {@code ID} 常量。
     */
    @Override
    public void registerRogueEvents(RogueEventRegistration registration) {
        registration.addEvent(MonsterEventType.ID, MonsterEventType::new);
        registration.addEvent(ShopEventType.ID, ShopEventType.class);
        registration.addEvent(BossEventType.ID, new BossEventType());
        registration.addEvent(HealEventType.ID, HealEventType::new);
        registration.addEvent(RewardEventType.ID, RewardEventType.class);
    }

    /**
     * 注册代码默认关卡。
     * <p>
     * 这里注册的是默认数据，资源包里同 id 的 JSON 会覆盖这些默认关卡。
     */
    @Override
    public void registerProgress(ProgressRegistration registration) {
        registration.addProgress(Beyond.asResource("tutorial"), new TutorialProgress());
        registration.addProgressBuilder(Beyond.asResource("forest"), ForestProgress::new);
    }

    /**
     * 初始化新 RogueData 默认装载的 Cap。
     * <p>
     * 这里不是注册 Cap 类型，只是声明新肉鸽数据默认启用哪些已注册 Cap。
     */
    @Override
    public void initRogueCaps(RogueCapInit registration) {
        registration.initCap(ProgressStartCap.ID);
        registration.initCap(PlayerInGameCap.ID);
        registration.initCap(NodeCap.ID);
        registration.initCap(NodeZoneEnterCap.ID);
        registration.initCap(RogueInitCap.ID);
        registration.initCap(RogueProgressFinishCap.ID);
        registration.initCap(PlayerProgressFinishCap.ID);
    }
}
