package org.biotech.api.system.trait.core;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.biotech.api.init.TraitInit;
import org.biotech.api.util.IActive;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.List;

/**
 * 词条基类
 */
public interface ITrait extends IActive {



    /**
     * 唯一id
     * @return
     */
    ResourceLocation getId();

    /**
     * 描述文本
     * @return
     */
    List<MutableComponent> getUniqueInfo();


    /**
     * 给一些方法和属性提供数据支持
     * @return
     */
    float getValue();


    default Component getDisplayName(){
        return Component.translatable("trait.biotech." + getId().getPath() + ".name");
    }


    default ResourceLocation getTexture(){
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/health_boost.png");
    }


    /**
     * 生成槽位级唯一 modifier id，避免不同词条/槽位相互覆盖。
     */
    default ResourceLocation getModifierId(CurioAttributeModifierEvent event) {
        ResourceLocation traitId = getId();
        ResourceLocation slotId = event.getId();
        String slotPath = slotId.getNamespace() + "_" + slotId.getPath().replace('/', '_');
        return ResourceLocation.fromNamespaceAndPath(
                traitId.getNamespace(),
                "trait/" + traitId.getPath() + "/" + slotPath
        );
    }


    static boolean isEmpty(ITrait trait) {
        return trait == null;
    }

    // 空词条的标识符
    ResourceLocation EMPTY_TRAIT_ID = ResourceLocation.fromNamespaceAndPath("biotech", "empty_trait");

    // CODEC - 通过 ResourceLocation 序列化
    Codec<ITrait> CODEC = ResourceLocation.CODEC.xmap(
        TraitInit::getTraitById,
        trait -> {
            if (trait == null || trait.getId() == null) {
                return EMPTY_TRAIT_ID;
            }
            return trait.getId();
        }
    );



    // STREAM_CODEC - 网络同步（使用 RegistryFriendlyByteBuf）
    StreamCodec<RegistryFriendlyByteBuf, ITrait> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ITrait decode(RegistryFriendlyByteBuf buf) {
            ResourceLocation id = buf.readResourceLocation();
            // 如果是空标识符，返回 null
            if (EMPTY_TRAIT_ID.equals(id)) {
                return null;
            }
            return TraitInit.getTraitById(id);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ITrait trait) {
            if (trait == null || trait.getId() == null) {
                buf.writeResourceLocation(EMPTY_TRAIT_ID);
            } else {
                buf.writeResourceLocation(trait.getId());
            }
        }
    };
}
