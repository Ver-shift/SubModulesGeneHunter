package org.galaxy.beyond.api.system.rogue.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.rogue.ProgressType;

import java.util.HashMap;
import java.util.Map;

@Data
public class RogueDefinition {
    /**
     * 关卡数据
     */
    private final Map<Identifier,ProgressDefinition> rogueProgress = new HashMap<>();

    public static final Codec<RogueDefinition> CODEC = RecordCodecBuilder.create();
    public static final StreamCodec<RegistryFriendlyByteBuf,RogueDefinition> STREAM_CODEC = aa;

}
