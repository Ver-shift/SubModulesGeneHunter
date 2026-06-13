package org.galaxy.beyond.api.init;

import com.mojang.serialization.Codec;
import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib2.plugin.LDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.rogue.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@LDLibPlugin
public class BeyondLDLibPlugin implements ILDLibPlugin {

    private static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.INT_STREAM.comapFlatMap(stream -> {
        int[] values = stream.toArray();
        return values.length == 2
                ? com.mojang.serialization.DataResult.success(new ChunkPos(values[0], values[1]))
                : com.mojang.serialization.DataResult.error(() -> "Expected two ints for ChunkPos");
    }, pos -> java.util.stream.IntStream.of(pos.x, pos.z));

    private static final StreamCodec<io.netty.buffer.ByteBuf, ChunkPos> CHUNK_POS_STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, pos -> pos.x,
                    ByteBufCodecs.INT, pos -> pos.z,
                    ChunkPos::new
            );

    @Override
    public void onLoad() {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(ChunkPos.class)
                .codec(CHUNK_POS_CODEC)
                .streamCodec(CHUNK_POS_STREAM_CODEC)
                .copyMark(pos -> new ChunkPos(pos.x, pos.z))
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(BlockPos.class)
                .codec(BlockPos.CODEC)
                .streamCodec(BlockPos.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class) ResourceKey.class)
                .codec((com.mojang.serialization.Codec) ResourceKey.codec(net.minecraft.core.registries.Registries.DIMENSION))
                .streamCodec((net.minecraft.network.codec.StreamCodec) ResourceKey.streamCodec(net.minecraft.core.registries.Registries.DIMENSION))
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class) RogueEventType.class)
                .codec(RogueEventType.CODEC.codec())
                .streamCodec(RogueEventType.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class) NodeColor.class)
                .codec(NodeColor.CODEC.codec())
                .streamCodec(NodeColor.STREAM_CODEC)
                .codecMark()
                .build());
    }
}
