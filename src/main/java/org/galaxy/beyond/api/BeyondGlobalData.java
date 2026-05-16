package org.galaxy.beyond.api;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import lombok.Data;
import org.galaxy.beyond.api.system.random.RogueRandom;
import org.galaxy.beyond.api.system.rogue.definition.RogueDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BeyondGlobalData implements IPersistedSerializable {

    @DescSynced
    @Persisted(subPersisted = true)
    private RogueDefinition rogueDefinition = new RogueDefinition();

    @DescSynced
    @Persisted(subPersisted = true)
    private RogueRandom rogueRandom = new RogueRandom();

    @DescSynced
    @Persisted
    private ResourceKey<Level> rougeLevel = Level.OVERWORLD;

    @DescSynced
    @Persisted
    private final Map<ResourceKey<Level>, BeyondDimensionData> dimensionDataMap = new ConcurrentHashMap<>();

    public static final MapCodec<BeyondGlobalData> CODEC = PersistedParser.createMapCodec(BeyondGlobalData::new);
    public static final StreamCodec<ByteBuf, BeyondGlobalData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondGlobalData::new);

    public BeyondDimensionData getOrCreateDimensionData(ResourceKey<Level> dimension) {
        return dimensionDataMap.computeIfAbsent(dimension, k -> new BeyondDimensionData());
    }
}
