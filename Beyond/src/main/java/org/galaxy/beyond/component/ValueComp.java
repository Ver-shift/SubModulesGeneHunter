package org.galaxy.beyond.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.galaxy.beyond.api.init.BeyondComponentInit;

public record ValueComp(int value) {



    public static int get(ItemStack stack) {
        ValueComp comp = stack.get(BeyondComponentInit.ITEM_VALUE.get());
        return comp != null ? comp.value() : 0;
    }

    public static void set(ItemStack stack, int value) {
        stack.set(BeyondComponentInit.ITEM_VALUE.get(), new ValueComp(value));
    }

    public static boolean has(ItemStack stack) {
        return stack.has(BeyondComponentInit.ITEM_VALUE.get());
    }

    public static final Codec<ValueComp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("value").forGetter(ValueComp::value)
    ).apply(instance, ValueComp::new
    ));

    public static final StreamCodec<RegistryFriendlyByteBuf,ValueComp> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
