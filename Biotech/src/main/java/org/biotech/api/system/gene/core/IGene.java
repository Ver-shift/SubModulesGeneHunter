package org.biotech.api.system.gene.core;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.biotech.Biotech;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.system.gene.GeneConfigBuilder;
import org.biotech.api.init.GeneInit;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public interface IGene extends ICurioItem {

    ResourceLocation getID();
    GeneConfigBuilder getConfigBuilder();

    default Component getDisplayName() {
        return Component.translatable("gene.biotech." + getID().getPath() + ".name");
    }



    default void tick(SlotContext slotContext, ItemStack stack){

    }
    
    default void geneOnEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {

    }

    default void onUnequip(SlotContext slotContext, ItemStack stack) {}

    default boolean geneCanEquip(SlotContext slotContext, ItemStack stack) { return true; }
    
    default boolean geneCanUnequip(SlotContext slotContext, ItemStack stack) { return true; }


    //======封装=======//
    @Override
    default void curioTick(SlotContext slotContext, ItemStack stack) {
        tick(slotContext, stack);
    }

    @Override
    default void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        geneOnEquip(slotContext,prevStack,stack);
    }

    @Override
    default void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        onUnequip(slotContext, stack);
    }

    @Override
    default boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return geneCanEquip(slotContext, stack);
    }

    @Override
    default boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return geneCanUnequip(slotContext, stack);
    }

    // CODEC - 通过 ResourceLocation 序列化
    Codec<IGene> CODEC = ResourceLocation.CODEC.xmap(
        GeneInit::getGeneById,  // ResourceLocation -> IGene
        IGene::getID            // IGene -> ResourceLocation
    );

    // 空基因的标识符
    ResourceLocation EMPTY_GENE_ID = Biotech.asResource("empty_gene_2");

    // STREAM_CODEC - 网络同步（支持 null，EMPTY 实例使用 EMPTY_GENE_ID）
    StreamCodec<RegistryFriendlyByteBuf, IGene> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, IGene>() {
        @Override
        public IGene decode(RegistryFriendlyByteBuf buf) {
            return GeneInit.getGeneById(buf.readResourceLocation());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, IGene gene) {
            buf.writeResourceLocation(gene != null ? gene.getID() : EMPTY_GENE_ID);
        }
    };


}
