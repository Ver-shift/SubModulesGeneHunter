package org.galaxy.beyond.api.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondItemInit;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 物品模型 JSON 直接输出，不依赖 NeoForge client model API。
 */
public class BeyondItemModelProvider implements DataProvider {

    private final PackOutput output;

    public BeyondItemModelProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        String name = BuiltInRegistries.ITEM.getKey(BeyondItemInit.LOOT_BAG.get()).getPath();
        Path dir = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(Beyond.MODID).resolve("models").resolve("item");
        return save(cache, dir, name, generated(name));
    }

    private static JsonObject generated(String name) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject tex = new JsonObject();
        tex.addProperty("layer0", Beyond.MODID + ":item/placeholder");
        root.add("textures", tex);
        return root;
    }

    private static CompletableFuture<?> save(CachedOutput cache, Path dir, String name, JsonObject json) {
        return DataProvider.saveStable(cache, json, dir.resolve(name + ".json"));
    }

    @Override
    public String getName() {
        return "Beyond Item Models";
    }
}
