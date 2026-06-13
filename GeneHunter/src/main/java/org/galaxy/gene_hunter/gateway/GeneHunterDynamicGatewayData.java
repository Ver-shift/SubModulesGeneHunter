package org.galaxy.gene_hunter.gateway;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class GeneHunterDynamicGatewayData implements IPersistedSerializable {

    @Persisted
    @ReadOnlyManaged(serializeMethod = "gatewaysSerialize", deserializeMethod = "gatewaysDeserialize")
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

    public CompoundTag gatewaysSerialize(Map<ResourceLocation, String> map) {
        ListTag keys = new ListTag();
        ListTag values = new ListTag();
        map.forEach((id, json) -> {
            keys.add(StringTag.valueOf(id.toString()));
            values.add(StringTag.valueOf(json));
        });

        CompoundTag tag = new CompoundTag();
        tag.put("keys", keys);
        tag.put("values", values);
        return tag;
    }

    public Map<ResourceLocation, String> gatewaysDeserialize(CompoundTag tag) {
        Map<ResourceLocation, String> map = new LinkedHashMap<>();
        ListTag keys = tag.getList("keys", Tag.TAG_STRING);
        ListTag values = tag.getList("values", Tag.TAG_STRING);
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
            map.put(ResourceLocation.parse(keys.getString(i)), values.getString(i));
        }
        return map;
    }
}
