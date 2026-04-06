package org.galaxylib.api.system.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.galaxylib.api.system.loot.data.GeneLootTableData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LevelLootData extends SavedData {
    public static final String DATA_NAME = "galaxylib_loot_tables";
    private static final String TAG_TABLES = "tables";

    private static final Codec<Map<ResourceLocation, GeneLootTableData>> TABLES_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, GeneLootTableData.CODEC);

    private static final SavedData.Factory<LevelLootData> FACTORY =
            new SavedData.Factory<>(LevelLootData::create, LevelLootData::load);

    private Map<ResourceLocation, GeneLootTableData> tables = new HashMap<>();

    public static LevelLootData create() {
        return new LevelLootData();
    }

    public static LevelLootData load(CompoundTag tag, HolderLookup.Provider provider) {
        LevelLootData data = new LevelLootData();
        if (!tag.contains(TAG_TABLES)) {
            return data;
        }

        DataResult<Map<ResourceLocation, GeneLootTableData>> parsed =
                TABLES_CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_TABLES));
        parsed.result().ifPresent(result -> data.tables = new HashMap<>(result));
        return data;
    }

    public static LevelLootData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return create();
        }
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Map<ResourceLocation, GeneLootTableData> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(tables));
    }

    public void setTables(Map<ResourceLocation, GeneLootTableData> newTables) {
        Map<ResourceLocation, GeneLootTableData> normalized =
                newTables == null ? new HashMap<>() : new HashMap<>(newTables);
        if (Objects.equals(this.tables, normalized)) {
            return;
        }
        this.tables = normalized;
        setDirty();
    }

    public GeneLootTableData getTable(ResourceLocation id) {
        return tables.get(id);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        DataResult<net.minecraft.nbt.Tag> encoded = TABLES_CODEC.encodeStart(NbtOps.INSTANCE, tables);
        encoded.result().ifPresent(tag -> compoundTag.put(TAG_TABLES, tag));
        return compoundTag;
    }
}
