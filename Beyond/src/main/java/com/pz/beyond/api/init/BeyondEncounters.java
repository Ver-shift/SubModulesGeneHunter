package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.progress.encounter.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

/**
 * 遭遇类型注册
 * 每个遭遇都是独立注册的类
 */
public class BeyondEncounters {

    public static final ResourceKey<Registry<EncounterType>> ENCOUNTER_REGISTRY_KEY =
        ResourceKey.createRegistryKey(Beyond.asResource("encounters"));

    public static final Registry<EncounterType> ENCOUNTER_REGISTRY = new RegistryBuilder<>(ENCOUNTER_REGISTRY_KEY).create();

    public static final DeferredRegister<EncounterType> ENCOUNTERS =
        DeferredRegister.create(ENCOUNTER_REGISTRY_KEY, Beyond.MODID);

    // ==================== 资源类型（Harvest）====================
    // 绿色：有趣的事件或者解密
    public static final Supplier<EncounterType> PUZZLE = ENCOUNTERS.register(PuzzleEncounter.ID.getPath(), PuzzleEncounter::new);
    // 橙色：普通的怪物
    public static final Supplier<EncounterType> NORMAL_MONSTER = ENCOUNTERS.register(NormalMonsterEncounter.ID.getPath(), NormalMonsterEncounter::new);
    // 红色：精英怪挑战
    public static final Supplier<EncounterType> ELITE_MONSTER = ENCOUNTERS.register(EliteMonsterEncounter.ID.getPath(), EliteMonsterEncounter::new);

    // ==================== 休息类型（Repose）====================
    // 绿色：篝火，纯粹的回复血量
    public static final Supplier<EncounterType> BONFIRE = ENCOUNTERS.register(BonfireEncounter.ID.getPath(), BonfireEncounter::new);
    // 橙色：普通商店
    public static final Supplier<EncounterType> NORMAL_SHOP = ENCOUNTERS.register(NormalShopEncounter.ID.getPath(), NormalShopEncounter::new);
    // 红色：诅咒商店
    public static final Supplier<EncounterType> CURSED_SHOP = ENCOUNTERS.register(CursedShopEncounter.ID.getPath(), CursedShopEncounter::new);

    // ==================== Boss类型（Climax）====================
    // Boss 类型使用颜色参数（都是商店+Boss）
    // 绿色
    public static final Supplier<EncounterType> BOSS_GREEN = ENCOUNTERS.register(BossEncounter.ID.getPath() + "_green",
        () -> new BossEncounter(NodeColor.GREEN));
    // 橙色
    public static final Supplier<EncounterType> BOSS_ORANGE = ENCOUNTERS.register(BossEncounter.ID.getPath() + "_orange",
        () -> new BossEncounter(NodeColor.ORANGE));
    // 红色
    public static final Supplier<EncounterType> BOSS_RED = ENCOUNTERS.register(BossEncounter.ID.getPath() + "_red",
        () -> new BossEncounter(NodeColor.RED));

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(ENCOUNTER_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        ENCOUNTERS.register(eventBus);
    }

    public static EncounterType getById(ResourceLocation id) {
        return ENCOUNTER_REGISTRY.get(id);
    }
}
