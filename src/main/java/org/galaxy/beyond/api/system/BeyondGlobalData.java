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
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import lombok.Data;
import org.galaxy.beyond.api.config.RogueConfig;
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

    @Persisted(subPersisted = true)
    private RogueConfig rogueConfig = new RogueConfig();

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
        m.keySet().forEach(k -> keys.add(StringTag.valueOf(k.identifier().toString())));
        var c = new CompoundTag();
        c.put("keys", keys);
        return c;
    }

    public Map<ResourceKey<Level>, BeyondDimensionData> dimensionDataMapDeserialize(CompoundTag c) {
        var m = new ConcurrentHashMap<ResourceKey<Level>, BeyondDimensionData>();
        var keys = c.getListOrEmpty("keys");
        for (Tag e : keys) {
            var loc = Identifier.parse(e.asString().orElse(""));
            m.put(ResourceKey.create(Registries.DIMENSION, loc), new BeyondDimensionData());
        }
        return m;
    }
}
