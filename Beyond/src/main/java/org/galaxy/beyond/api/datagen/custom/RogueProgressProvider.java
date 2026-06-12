package org.galaxy.beyond.api.datagen.custom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.pack.IdentifierTypeAdapter;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.*;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 肉鸽进度数据生成基类。
 * <p>
 * 子类负责注册 ProgressDefinition，本类负责提供 DSL 并写入 JSON。
 */
public abstract class RogueProgressProvider implements DataProvider {

    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new IdentifierTypeAdapter())
            .create();

    private final PackOutput output;

    public RogueProgressProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<Entry> entries = new ArrayList<>();
        registerProgress(entries);
        return CompletableFuture.allOf(entries.stream()
                .map(e -> save(cache, e.name, e.definition))
                .toArray(CompletableFuture[]::new));
    }

    /** 子类在这里注册关卡。 */
    protected abstract void registerProgress(List<Entry> entries);

    protected Entry entry(String name, ProgressDefinition definition) {
        return new Entry(name, definition);
    }

    /** 创建 EventTask：evt("monster", "boss") -> beyond:monster + beyond:boss。 */
    public static EventTask evt(String... ids) {
        List<ResourceLocation> list = new ArrayList<>();
        for (String id : ids) list.add(Beyond.asResource(id));
        return new EventTask(list);
    }

    /** 创建加权事件条目。 */
    public static EventRoll roll(int weight, EventTask task) {
        return EventRoll.of(weight, task);
    }

    /** 创建遭遇定义，参数按 EventTask、weight 交替传入。 */
    public static Encounter enc(EncounterType type, Object... events) {
        List<EventRoll> rolls = new ArrayList<>();
        for (int i = 0; i < events.length; ) {
            if (events[i] instanceof EventTask task) {
                int weight = (i + 1 < events.length && events[i + 1] instanceof Integer w) ? w : 1;
                rolls.add(roll(weight, task));
                i += (i + 1 < events.length && events[i + 1] instanceof Integer) ? 2 : 1;
            } else {
                i++;
            }
        }
        return Encounter.builder().type(type).events(rolls).build();
    }

    private CompletableFuture<?> save(CachedOutput cache, String name, ProgressDefinition def) {
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(Beyond.MODID)
                .resolve("rogue_progress")
                .resolve(name + ".json");
        return DataProvider.saveStable(cache, GSON.toJsonTree(def), path);
    }

    protected record Entry(String name, ProgressDefinition definition) {
    }
}
