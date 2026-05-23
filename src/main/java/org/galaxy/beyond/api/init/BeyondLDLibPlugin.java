package org.galaxy.beyond.api.init;

import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib2.plugin.LDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.config.RogueConfig;
import org.galaxy.beyond.api.system.BeyondDimensionData;
import org.galaxy.beyond.api.system.BeyondGlobalData;
import org.galaxy.beyond.api.system.BeyondMobData;
import org.galaxy.beyond.api.system.BeyondPlayerData;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.*;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueData;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureData;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneData;

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

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(BlockPos.class)
                .codec(BlockPos.CODEC)
                .streamCodec(BlockPos.STREAM_CODEC)
                .codecMark()
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

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(RogueData.class)
                .codec(RogueData.CODEC.codec())
                .streamCodec(RogueData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(ProgressType.class)
                .codec(ProgressType.CODEC.codec())
                .streamCodec(ProgressType.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(RogueNodeData.class)
                .codec(RogueNodeData.CODEC.codec())
                .streamCodec(RogueNodeData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(NodeData.class)
                .codec(NodeData.CODEC.codec())
                .streamCodec(NodeData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(EventTask.class)
                .codec(EventTask.CODEC.codec())
                .streamCodec(EventTask.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(RogueCapData.class)
                .codec(RogueCapData.CODEC.codec())
                .streamCodec(RogueCapData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class) RogueEventType.class)
                .codec(RogueEventType.CODEC.codec())
                .streamCodec(RogueEventType.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(LevelZoneData.class)
                .codec(LevelZoneData.CODEC.codec())
                .streamCodec(LevelZoneData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(ZoneData.class)
                .codec(ZoneData.CODEC.codec())
                .streamCodec(ZoneData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(BeyondGlobalData.class)
                .codec(BeyondGlobalData.CODEC.codec())
                .streamCodec(BeyondGlobalData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(BeyondPlayerData.class)
                .codec(BeyondPlayerData.CODEC.codec())
                .streamCodec(BeyondPlayerData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(BeyondMobData.class)
                .codec(BeyondMobData.CODEC.codec())
                .streamCodec(BeyondMobData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(RogueConfig.class)
                .codec(RogueConfig.CODEC.codec())
                .streamCodec(RogueConfig.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(SafeZoneStructureData.class)
                .codec(SafeZoneStructureData.CODEC.codec())
                .streamCodec(SafeZoneStructureData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(PlayerRogueData.class)
                .codec(PlayerRogueData.CODEC.codec())
                .streamCodec(PlayerRogueData.STREAM_CODEC)
                .codecMark()
                .build());

        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class) NodeColor.class)
                .codec(NodeColor.CODEC.codec())
                .streamCodec(NodeColor.STREAM_CODEC)
                .codecMark()
                .build());
    }
}
