package org.galaxy.beyond.api.datagen.custom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.pack.IdentifierTypeAdapter;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 刷怪定义数据生成基类。
 */
public abstract class SpawnDefinitionProvider implements DataProvider {

    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new IdentifierTypeAdapter().nullSafe())
            .create();

    private final PackOutput output;

    public SpawnDefinitionProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<Entry> entries = new ArrayList<>();
        registerDefinitions(entries);
        return CompletableFuture.allOf(entries.stream()
                .map(e -> save(cache, e.id, e.definition))
                .toArray(CompletableFuture[]::new));
    }

    protected abstract void registerDefinitions(List<Entry> entries);

    protected Entry entry(ResourceLocation id, SpawnDefinition definition) {
        if (definition.getId() == null) {
            definition.setId(id);
        }
        return new Entry(id, definition);
    }

    private CompletableFuture<?> save(CachedOutput cache, ResourceLocation id, SpawnDefinition definition) {
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(id.getNamespace())
                .resolve("spawn_definitions")
                .resolve(id.getPath() + ".json");
        JsonObject json = GSON.toJsonTree(definition).getAsJsonObject();
        json.entrySet().removeIf(entry -> entry.getValue().isJsonNull());
        return DataProvider.saveStable(cache, json, path);
    }

    protected record Entry(ResourceLocation id, SpawnDefinition definition) {
    }
}
