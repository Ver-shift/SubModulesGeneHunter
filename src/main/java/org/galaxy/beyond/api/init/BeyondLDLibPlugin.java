package org.galaxy.beyond.api.init;

import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib2.plugin.LDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.BeyondDimensionData;

@SuppressWarnings({"unchecked", "rawtypes"})
@LDLibPlugin
public class BeyondLDLibPlugin implements ILDLibPlugin {

    @Override
    public void onLoad() {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(ChunkPos.class)
                .codec(ChunkPos.CODEC)
                .streamCodec(ChunkPos.STREAM_CODEC)
                .copyMark(pos -> new ChunkPos(pos.x(), pos.z()))
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(BeyondDimensionData.class)
                .codec(BeyondDimensionData.CODEC_DIRECT)
                .streamCodec(BeyondDimensionData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class) ResourceKey.class)
                .codec((com.mojang.serialization.Codec) ResourceKey.codec(net.minecraft.core.registries.Registries.DIMENSION))
                .streamCodec((net.minecraft.network.codec.StreamCodec) ResourceKey.streamCodec(net.minecraft.core.registries.Registries.DIMENSION))
                .codecMark()
                .build());
    }
}
