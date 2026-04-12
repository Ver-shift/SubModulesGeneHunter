package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;


public class SaveLevelData extends SavedData {
    public static final String DATA_NAME = "beyond_level_zone_data";
    private static final String TAG_LEVEL_ZONE_MAP = "level_zone_data_map";

    private static final Codec<Map<ResourceLocation, LevelZoneData>> LEVEL_ZONE_MAP_CODEC =
        Codec.unboundedMap(ResourceLocation.CODEC, LevelZoneData.CODEC);

    private static final SavedData.Factory<SaveLevelData> FACTORY =
        new SavedData.Factory<>(SaveLevelData::create, SaveLevelData::load);

    @Getter
    public HashMap<ResourceKey<Level>, LevelZoneData> levelZoneDataMap = new HashMap<>();

    public static SaveLevelData create() {
        return new SaveLevelData();
    }

    public static SaveLevelData load(CompoundTag tag, HolderLookup.Provider provider) {
        SaveLevelData data = new SaveLevelData();
        if (!tag.contains(TAG_LEVEL_ZONE_MAP)) {
            return data;
        }

        DataResult<Map<ResourceLocation, LevelZoneData>> parsed =
            LEVEL_ZONE_MAP_CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_LEVEL_ZONE_MAP));
        parsed.result().ifPresent(map -> {
            for (Map.Entry<ResourceLocation, LevelZoneData> entry : map.entrySet()) {
                ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, entry.getKey());
                data.levelZoneDataMap.put(dimensionKey, entry.getValue());
            }
        });
        return data;
    }

    public static SaveLevelData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return create();
        }
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public LevelZoneData getOrCreateLevelData(ResourceKey<Level> dimension) {
        LevelZoneData data = levelZoneDataMap.computeIfAbsent(dimension, key -> {
            LevelZoneData created = new LevelZoneData();
            created.setDimension(key);
            return created;
        });
        setDirty();
        return data;
    }

    public void putLevelData(ResourceKey<Level> dimension, LevelZoneData data) {
        levelZoneDataMap.put(dimension, data);
        setDirty();
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        Map<ResourceLocation, LevelZoneData> serialized = new HashMap<>();
        for (Map.Entry<ResourceKey<Level>, LevelZoneData> entry : levelZoneDataMap.entrySet()) {
            serialized.put(entry.getKey().location(), entry.getValue());
        }

        DataResult<net.minecraft.nbt.Tag> encoded = LEVEL_ZONE_MAP_CODEC.encodeStart(NbtOps.INSTANCE, serialized);
        encoded.result().ifPresent(resultTag -> tag.put(TAG_LEVEL_ZONE_MAP, resultTag));
        return tag;
    }
}
