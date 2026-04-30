package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

/**
 * 节点唯一键：结构 ResourceLocation + 结构中心 ChunkPos。
 * <p>
 * 类似 {@link ChunkPos} 的值对象语义：不可变、可作 HashMap key。
 * 序列化字符串形式：{@code "namespace:path@cx,cz"}，示例：{@code "minecraft:village_plains@12,-7"}。
 * Minecraft 原版没有现成的"结构 + 坐标"组合类，故自建。
 */
public final class StructureKey {

    /** 分隔符：结构 id 与坐标之间 */
    public static final char SEPARATOR = '@';

    public static final StructureKey EMPTY = new StructureKey(
            ResourceLocation.fromNamespaceAndPath("beyond", "empty"), new ChunkPos(0, 0));

    private final ResourceLocation structure;
    private final ChunkPos center;

    public StructureKey(ResourceLocation structure, ChunkPos center) {
        this.structure = structure == null ? EMPTY.structure : structure;
        this.center = center == null ? EMPTY.center : center;
    }

    public StructureKey(ResourceLocation structure, long chunkKey) {
        this(structure, new ChunkPos(chunkKey));
    }

    public ResourceLocation structure() {
        return structure;
    }

    public ChunkPos center() {
        return center;
    }

    public long centerLong() {
        return center.toLong();
    }

    /** 格式：{@code namespace:path@cx,cz} */
    @Override
    public String toString() {
        return structure + String.valueOf(SEPARATOR) + center.x + "," + center.z;
    }

    /**
     * 从字符串还原。失败返回 {@link #EMPTY}。
     */
    public static StructureKey parse(String raw) {
        if (raw == null || raw.isEmpty()) return EMPTY;
        int at = raw.lastIndexOf(SEPARATOR);
        if (at <= 0 || at >= raw.length() - 1) return EMPTY;
        String idPart = raw.substring(0, at);
        String posPart = raw.substring(at + 1);
        int comma = posPart.indexOf(',');
        if (comma <= 0 || comma >= posPart.length() - 1) return EMPTY;
        ResourceLocation id = ResourceLocation.tryParse(idPart);
        if (id == null) return EMPTY;
        try {
            int cx = Integer.parseInt(posPart.substring(0, comma));
            int cz = Integer.parseInt(posPart.substring(comma + 1));
            return new StructureKey(id, new ChunkPos(cx, cz));
        } catch (NumberFormatException e) {
            return EMPTY;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructureKey other)) return false;
        return Objects.equals(structure, other.structure)
                && center.x == other.center.x && center.z == other.center.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, center.x, center.z);
    }


    //==================== Codec ====================
    /** 记录式 Codec，用于列表 / 字段嵌套场景 */
    public static final Codec<StructureKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(StructureKey::structure),
            Codec.LONG.fieldOf("center").forGetter(StructureKey::centerLong)
    ).apply(instance, StructureKey::new));

    /** 字符串式 Codec，用于做 Map key */
    public static final Codec<StructureKey> STRING_CODEC = Codec.STRING.xmap(
            StructureKey::parse,
            StructureKey::toString
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureKey> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StructureKey::structure,
            ByteBufCodecs.VAR_LONG, StructureKey::centerLong,
            StructureKey::new
    );
}
