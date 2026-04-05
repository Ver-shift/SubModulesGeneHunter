package org.biotech.component;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Hash;
import lombok.Data;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
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


    @ReadOnlyManaged(serializeMethod = "writeCategoryInfo", deserializeMethod = "readCategoryInfo")
    private List<String> value3= new ArrayList<>();

    @ReadOnlyManaged(serializeMethod = "writeMapCategoryInfo", deserializeMethod = "readMapCategoryInfo")
    private HashMap<ResourceLocation, String> value4= new HashMap<>();

    public Example(){

    }
    private Tag writeCategoryInfo(List<String> value) {
        return IntTag.valueOf(value.size());
    }

    private List<String> readCategoryInfo(IntTag tag) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < tag.getAsInt(); i++) {
            list.add("");
        }
        return list;
    }

    private Tag writeMapCategoryInfo(HashMap<ResourceLocation, String> value) {
        return IntTag.valueOf(value.size());
    }

    private HashMap<ResourceLocation, String> readMapCategoryInfo(IntTag tag) {
        HashMap<ResourceLocation, String> map = new HashMap<>();
        for (int i = 0; i < tag.getAsInt(); i++) {
            map.put(ResourceLocation.fromNamespaceAndPath("biotech_new_age", "entry_" + i), "");
        }
        return map;
    }

}
