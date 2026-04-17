package com.pz.beyond.api.system.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.node.NodeEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个节点的任务列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTask {

    private List<NodeEventType> events = new ArrayList<>();

    public static final Codec<EventTask> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            NodeEventType.CODEC.listOf().fieldOf("events").forGetter(EventTask::getEvents)
        ).apply(instance, EventTask::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EventTask> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, NodeEventType.STREAM_CODEC),
        EventTask::getEvents,
        EventTask::new
    );
}
