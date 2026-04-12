package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.core.IRuleContainer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 区域的数据，每个区域都有一个数据
 */
@Data
public class ZoneData implements IRuleContainer {

    public static final String ZONE_ID = "id";
    public static final String LISTENERS = "listeners";
    public static final String CHUNK_KEYS = "chunk_keys";

    private AbstractZone zone;
    private volatile List<RuleData> listeners = new ArrayList<>();
    private final LongOpenHashSet chunkKeys = new LongOpenHashSet();

    public static final Codec<ZoneData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf(ZONE_ID).forGetter(ZoneData::getZoneId),
        RuleData.CODEC.listOf().optionalFieldOf(LISTENERS, List.of()).forGetter(ZoneData::getListeners),
        Codec.LONG.listOf().optionalFieldOf(CHUNK_KEYS, List.of()).forGetter(ZoneData::getChunkKeyList)

    ).apply(builder, ZoneData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ZoneData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ZoneData decode(RegistryFriendlyByteBuf buf) {
            AbstractZone zone = BeyondZoneInit.getZoneById(buf.readResourceLocation());

            int listenerSize = buf.readVarInt();
            List<RuleData> listeners = new ArrayList<>(listenerSize);
            for (int i = 0; i < listenerSize; i++) {
                listeners.add(RuleData.STREAM_CODEC.decode(buf));
            }

            int chunkKeySize = buf.readVarInt();
            List<Long> chunkKeys = new ArrayList<>(chunkKeySize);
            for (int i = 0; i < chunkKeySize; i++) {
                chunkKeys.add(buf.readLong());
            }

            return new ZoneData(zone, listeners, chunkKeys);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ZoneData data) {
            buf.writeResourceLocation(data.getZoneId());

            List<RuleData> listeners = data.listeners == null ? List.of() : data.listeners;
            buf.writeVarInt(listeners.size());
            for (RuleData listener : listeners) {
                RuleData.STREAM_CODEC.encode(buf, listener);
            }

            List<Long> chunkKeys = data.getChunkKeyList();
            buf.writeVarInt(chunkKeys.size());
            for (Long key : chunkKeys) {
                buf.writeLong(key);
            }
        }
    };

    public ZoneData(AbstractZone zone) {
        this.zone = zone;
    }

    public ZoneData(ResourceLocation zoneId, List<RuleData> listeners, List<Long> chunkKeys) {
        this(BeyondZoneInit.getZoneById(zoneId), listeners, chunkKeys);
    }

    public ZoneData(AbstractZone zone, List<RuleData> listeners, List<Long> chunkKeys) {
        this.zone = zone;
        if (listeners != null) {
            this.listeners = new ArrayList<>(listeners);
        }
        if (chunkKeys != null) {
            this.chunkKeys.addAll(chunkKeys);
        }
    }

    public ResourceLocation getZoneId() {
        return BeyondZoneInit.getZoneId(zone);
    }

    private List<Long> getChunkKeyList() {
        List<Long> keys = new ArrayList<>(chunkKeys.size());
        chunkKeys.forEach((long key) -> keys.add(key));
        return keys;
    }


    public void addListener(RuleData listener) {
        this.listeners.add(listener);
    }
    public void removeListener(RuleData listener) {
        this.listeners.remove(listener);
    }

    public void clearListeners() {
        this.listeners.clear();
    }



}
