package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.progress.SceneType;
import com.pz.beyond.progress.scene.BossScene;
import com.pz.beyond.progress.scene.HarvestScene;
import com.pz.beyond.progress.scene.ReposeScene;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

/**
 * 场景类型注册
 */
public class BeyondSceneTypes {

    public static final ResourceKey<Registry<SceneType>> SCENE_TYPE_REGISTRY_KEY =
        ResourceKey.createRegistryKey(Beyond.asResource("scene_types"));

    public static final Registry<SceneType> SCENE_TYPE_REGISTRY = new RegistryBuilder<>(SCENE_TYPE_REGISTRY_KEY).create();

    public static final DeferredRegister<SceneType> SCENE_TYPES =
        DeferredRegister.create(SCENE_TYPE_REGISTRY_KEY, Beyond.MODID);

    // 三种场景类型（使用现有的类）
    public static final Supplier<SceneType> HARVEST = SCENE_TYPES.register("harvest", HarvestScene::new);

    public static final Supplier<SceneType> REPOSE = SCENE_TYPES.register("repose", ReposeScene::new);

    public static final Supplier<SceneType> CLIMAX = SCENE_TYPES.register("climax", BossScene::new);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(SCENE_TYPE_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        SCENE_TYPES.register(eventBus);
    }

    public static SceneType getById(ResourceLocation id) {
        return SCENE_TYPE_REGISTRY.get(id);
    }
}
