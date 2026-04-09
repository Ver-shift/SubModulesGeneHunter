package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.rule.IZoneRule;
import com.pz.beyond.api.system.zone.core.ISafeZoneRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public class SafeZone extends AbstractZone<ISafeZoneRule> {

    public static final ResourceLocation IDENTIFIER = Beyond.asResource("safe_zone");

    /**
     * Listener标识符的CODEC（用于持久化）
     */
    private static final Codec<IZoneRule> LISTENER_CODEC = ResourceLocation.CODEC.xmap(
        id -> new SafeZoneRule(id),  // 反序列化时创建listener
        IZoneRule::getIdentifier       // 序列化时获取id
    );

    /**
     * CODEC - 用于LevelAttachment序列化
     */
    public static final Codec<SafeZone> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.listOf().fieldOf("chunks").forGetter(SafeZone::getChunkKeyList),
            LISTENER_CODEC.listOf().optionalFieldOf("listeners", List.of()).forGetter(SafeZone::getListenerList)
        ).apply(instance, SafeZone::fromCodec)
    );

    /**
     * STREAM_CODEC - 用于网络同步
     */
    public static final StreamCodec<ByteBuf, SafeZone> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()),  // List<Long> 代替 long[]
        SafeZone::getChunkKeyList,                           // 返回 List<Long>
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
        SafeZone::getListenerIdList,
        SafeZone::fromStream
    );

    public SafeZone() {
        super(IDENTIFIER);
        this.addListener();
    }

    /**
     * CODEC反序列化工厂方法
     */
    @SuppressWarnings("unchecked")
    private static SafeZone fromCodec(List<Long> chunkKeys, List<IZoneRule> listeners) {
        SafeZone zone = new SafeZone();
        for (Long key : chunkKeys) {
            zone.addChunk(ChunkPos.getX(key), ChunkPos.getZ(key));
        }
        if (!listeners.isEmpty()) {
            zone.setListeners((ISafeZoneRule[]) listeners.toArray(new IZoneRule[0]));
        }
        return zone;
    }

    /**
     * STREAM_CODEC反序列化工厂方法
     */
    @SuppressWarnings("unchecked")
    private static SafeZone fromStream(List<Long> chunkKeys, List<ResourceLocation> listenerIds) {
        SafeZone zone = new SafeZone();
        for (Long key : chunkKeys) {
            zone.addChunk(ChunkPos.getX(key), ChunkPos.getZ(key));
        }
        // 网络同步时根据ID重建listeners
        ISafeZoneRule[] listeners = new ISafeZoneRule[listenerIds.size()];
        for (int i = 0; i < listenerIds.size(); i++) {
            listeners[i] = new SafeZoneRule(listenerIds.get(i));
        }
        zone.setListeners(listeners);
        return zone;
    }

    /**
     * 获取chunkKey列表（用于序列化）
     */
    private List<Long> getChunkKeyList() {
        return new java.util.ArrayList<>(getChunkKeys());
    }

    /**
     * 获取listener列表（用于序列化）
     */
    private List<IZoneRule> getListenerList() {
        return List.of(getListeners());
    }

    /**
     * 获取listener ID列表（用于网络序列化）
     */
    private List<ResourceLocation> getListenerIdList() {
        IZoneRule[] listeners = getListeners();
        List<ResourceLocation> ids = new java.util.ArrayList<>(listeners.length);
        for (IZoneRule listener : listeners) {
            ids.add(listener.getIdentifier());
        }
        return ids;
    }

    /**
     * SafeZone专用Listener实现
     */
    private record SafeZoneRule(ResourceLocation identifier) implements IZoneRule {
        @Override
        public ResourceLocation getIdentifier() {
            return identifier;
        }


    }
}
