package org.biotech.api.system.trait.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.biotech.api.init.BiotechTraitInit;
import org.biotech.api.util.IActive;

import java.util.List;

/**
 * 词条基类
 */
public interface ITrait extends IActive {


    /**
     * 唯一id
     *
     * @return
     */
    ResourceLocation getId();

    /**
     * 描述文本
     *
     * @return
     */
    List<MutableComponent> getUniqueInfo();


    default Component getDisplayName() {
        return Component.translatable("trait.biotech." + getId().getPath() + ".name");
    }


    default ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/health_boost.png");
    }


    /**
     * 生成 trait 级稳定 modifier id，同一个 trait 共用一条玩家属性通道。
     */
    default ResourceLocation getModifierId() {
        ResourceLocation traitId = getId();
        return ResourceLocation.fromNamespaceAndPath(
                traitId.getNamespace(),
                "trait/" + traitId.getPath()
        );
    }

    default Holder<Attribute> getAttribute() {
        return null;
    }

    default double getAttributeValue(int traitCount) {
        return 0;
    }

    default AttributeModifier.Operation getAttributeOperation() {
        return AttributeModifier.Operation.ADD_VALUE;
    }

    @Override
    default void modifyAttributes(Player player, int traitCount) {
        Holder<Attribute> attributeHolder = getAttribute();
        if (attributeHolder == null) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(attributeHolder);
        if (attribute == null) {
            return;
        }

        attribute.addOrUpdateTransientModifier(
                new AttributeModifier(
                        getModifierId(),
                        getAttributeValue(traitCount),
                        getAttributeOperation()
                )
        );
    }

    @Override
    default void removeAttributes(Player player) {
        Holder<Attribute> attributeHolder = getAttribute();
        if (attributeHolder == null) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(attributeHolder);
        if (attribute != null) {
            attribute.removeModifier(getModifierId());
        }
    }


    static boolean isEmpty(ITrait trait) {
        return trait == null;
    }

    // 空词条的标识符
    ResourceLocation EMPTY_TRAIT_ID = ResourceLocation.fromNamespaceAndPath("biotech", "empty_trait");

    // CODEC - 通过 ResourceLocation 序列化
    Codec<ITrait> CODEC = ResourceLocation.CODEC.xmap(
            BiotechTraitInit::getTraitById,
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
            return BiotechTraitInit.getTraitById(id);
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
