package org.galaxy.gene_hunter.gateway;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class GeneHunterDynamicGatewayData implements IPersistedSerializable {

    @Persisted
    private Map<ResourceLocation, String> gateways = new LinkedHashMap<>();

    public static final MapCodec<GeneHunterDynamicGatewayData> CODEC = PersistedParser.createMapCodec(GeneHunterDynamicGatewayData::new);
    public static final StreamCodec<ByteBuf, GeneHunterDynamicGatewayData> STREAM_CODEC = PersistedParser.createStreamCodec(GeneHunterDynamicGatewayData::new);

    public void put(ResourceLocation id, String json) {
        gateways.put(id, json);
    }

    public String get(ResourceLocation id) {
        return gateways.get(id);
    }

    public Map<ResourceLocation, String> entries() {
        return new LinkedHashMap<>(gateways);
    }
}
