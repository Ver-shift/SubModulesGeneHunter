package org.biotech.component;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class Example implements IConfigurable, IPersistedSerializable {

    public static final Codec<Example> CODEC = PersistedParser.createCodec(Example::new);
    public static final StreamCodec<ByteBuf, Example> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);


    @DescSynced
    private int value1 = 0;


    @DescSynced
    private boolean value2 = false;


    private List<String> value3 = new ArrayList<>();

    private HashMap<ResourceLocation, String> value4 = new HashMap<>();

    public Example() {

    }

}
