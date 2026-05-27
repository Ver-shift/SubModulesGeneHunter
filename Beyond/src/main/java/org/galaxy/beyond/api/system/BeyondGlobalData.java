package org.galaxy.beyond.api.system;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import lombok.Data;
import org.galaxy.beyond.api.system.random.RogueRandom;
import org.galaxy.beyond.api.system.rogue.definition.RogueDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BeyondGlobalData implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    private RogueDefinition rogueDefinition = new RogueDefinition();

    @Persisted(subPersisted = true)
    private RogueRandom rogueRandom = new RogueRandom();

    @Persisted
    private ResourceKey<Level> rougeLevel = Level.OVERWORLD;

    // Map由PersistedParser直接序列化
    @Persisted
    @ReadOnlyManaged(serializeMethod = "dimensionDataMapSerialize", deserializeMethod = "dimensionDataMapDeserialize")
    private Map<ResourceKey<Level>, BeyondDimensionData> dimensionDataMap = new ConcurrentHashMap<>();

    public static final MapCodec<BeyondGlobalData> CODEC = PersistedParser.createMapCodec(BeyondGlobalData::new);
    public static final StreamCodec<ByteBuf, BeyondGlobalData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondGlobalData::new);

    public BeyondDimensionData getOrCreateDimensionData(ResourceKey<Level> dimension) {
        return dimensionDataMap.computeIfAbsent(dimension, k -> new BeyondDimensionData());
    }

    public CompoundTag dimensionDataMapSerialize(Map<ResourceKey<Level>, BeyondDimensionData> m) {
        var keys = new ListTag();
        var values = new ListTag();
        for (var e : m.entrySet()) {
            keys.add(StringTag.valueOf(e.getKey().location().toString()));
            BeyondDimensionData.CODEC_DIRECT.encodeStart(NbtOps.INSTANCE, e.getValue())
                    .result().ifPresent(values::add);
        }
        var c = new CompoundTag();
        c.put("keys", keys);
        c.put("values", values);
        return c;
    }

    public Map<ResourceKey<Level>, BeyondDimensionData> dimensionDataMapDeserialize(CompoundTag c) {
        var m = new ConcurrentHashMap<ResourceKey<Level>, BeyondDimensionData>();
        var keys = c.getList("keys", net.minecraft.nbt.Tag.TAG_STRING);
        var values = c.getList("values", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < keys.size(); i++) {
            Tag key = keys.get(i);
            var loc = ResourceLocation.parse(key.getAsString());
            BeyondDimensionData data = i < values.size()
                    ? BeyondDimensionData.CODEC_DIRECT.parse(NbtOps.INSTANCE, values.get(i)).result().orElseGet(BeyondDimensionData::new)
                    : new BeyondDimensionData();
            m.put(ResourceKey.create(Registries.DIMENSION, loc), data);
        }
        return m;
    }
}
