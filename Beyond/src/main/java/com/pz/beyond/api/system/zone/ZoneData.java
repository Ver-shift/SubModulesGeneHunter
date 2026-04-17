package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.core.IRuleContainer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static final String INITIALIZED = "initialized";

    private AbstractZone<?> zone;
    private volatile List<RuleData> listeners = new ArrayList<>();
    private final LongOpenHashSet chunkKeys = new LongOpenHashSet();
    /**
     * 是否已初始化（防止重复初始化）
     */
    private boolean initialized = false;

    public static final Codec<ZoneData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf(ZONE_ID).forGetter(ZoneData::getZoneId),
        RuleData.CODEC.listOf().optionalFieldOf(LISTENERS, List.of()).forGetter(ZoneData::getListeners),
        Codec.LONG.listOf().optionalFieldOf(CHUNK_KEYS, List.of()).forGetter(ZoneData::getChunkKeyList),
        Codec.BOOL.optionalFieldOf(INITIALIZED, false).forGetter(ZoneData::isInitialized)
    ).apply(builder, ZoneData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, AbstractZone<?>> ZONE_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AbstractZone<?> decode(RegistryFriendlyByteBuf buf) {
            return BeyondZoneInit.getZoneById(buf.readResourceLocation());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AbstractZone<?> zone) {
            buf.writeResourceLocation(BeyondZoneInit.getZoneId(zone));
        }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, List<RuleData>> LISTENER_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, RuleData.STREAM_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf, Long> LONG_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Long decode(RegistryFriendlyByteBuf buf) {
            return buf.readLong();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Long value) {
            buf.writeLong(value == null ? 0L : value);
        }
    };
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Long>> CHUNK_KEY_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, LONG_STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ZoneData> STREAM_CODEC = StreamCodec.composite(
        ZONE_STREAM_CODEC,
        ZoneData::getZone,
        LISTENER_LIST_STREAM_CODEC,
        ZoneData::getListenersSafe,
        CHUNK_KEY_LIST_STREAM_CODEC,
        ZoneData::getChunkKeyList,
        ByteBufCodecs.BOOL,
        ZoneData::isInitialized,
        ZoneData::new
    );

    public ZoneData(AbstractZone<?> zone) {
        this.zone = zone;
    }

    public ZoneData(ResourceLocation zoneId, List<RuleData> listeners, List<Long> chunkKeys) {
        this(BeyondZoneInit.getZoneById(zoneId), listeners, chunkKeys, false);
    }

    public ZoneData(ResourceLocation zoneId, List<RuleData> listeners, List<Long> chunkKeys, boolean initialized) {
        this(BeyondZoneInit.getZoneById(zoneId), listeners, chunkKeys, initialized);
    }

    public ZoneData(AbstractZone<?> zone, List<RuleData> listeners, List<Long> chunkKeys) {
        this(zone, listeners, chunkKeys, false);
    }

    public ZoneData(AbstractZone<?> zone, List<RuleData> listeners, List<Long> chunkKeys, boolean initialized) {
        this.zone = zone;
        if (listeners != null) {
            this.listeners = new ArrayList<>(listeners);
        }
        if (chunkKeys != null) {
            this.chunkKeys.addAll(chunkKeys);
        }
        this.initialized = initialized;
    }

    public ResourceLocation getZoneId() {
        return BeyondZoneInit.getZoneId(zone);
    }

    private List<Long> getChunkKeyList() {
        List<Long> keys = new ArrayList<>(chunkKeys.size());
        chunkKeys.forEach((long key) -> keys.add(key));
        return keys;
    }

    private List<RuleData> getListenersSafe() {
        return listeners == null ? List.of() : listeners;
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
