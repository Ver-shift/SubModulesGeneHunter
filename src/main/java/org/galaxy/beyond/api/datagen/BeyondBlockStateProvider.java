package org.galaxy.beyond.api.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondBlockInit;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 方块模型 / blockstate JSON 直接输出，不依赖 NeoForge client model API。
 */
public class BeyondBlockStateProvider implements DataProvider {

    private final PackOutput output;

    public BeyondBlockStateProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        String name = BuiltInRegistries.BLOCK.getKey(BeyondBlockInit.NODE_BLOCK.get()).getPath();
        Path bs = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(Beyond.MODID).resolve("blockstates");
        Path mb = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(Beyond.MODID).resolve("models").resolve("block");
        Path mi = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(Beyond.MODID).resolve("models").resolve("item");

        return CompletableFuture.allOf(
                save(cache, bs, name, blockstate(name)),
                save(cache, mb, name, cubeAll(name)),
                save(cache, mi, name, blockItemModel(name))
        );
    }

    private static JsonObject blockstate(String name) {
        JsonObject v = new JsonObject();
        JsonObject m = new JsonObject();
        m.addProperty("model", Beyond.MODID + ":block/" + name);
        v.add("", m);
        JsonObject root = new JsonObject();
        root.add("variants", v);
        return root;
    }

    private static JsonObject cubeAll(String name) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube_all");
        JsonObject tex = new JsonObject();
        tex.addProperty("all", Beyond.MODID + ":block/placeholder");
        root.add("textures", tex);
        return root;
    }

    private static JsonObject blockItemModel(String name) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", Beyond.MODID + ":block/" + name);
        return root;
    }

    private static CompletableFuture<?> save(CachedOutput cache, Path dir, String name, JsonObject json) {
        return DataProvider.saveStable(cache, json, dir.resolve(name + ".json"));
    }

    @Override
    public String getName() {
        return "Beyond BlockStates & Models";
    }
}
