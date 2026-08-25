package org.galaxy.gene_hunter.api.system.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Persistent weapon category used for the four supported shape tooltips. */
public record WeaponClassComponent(WeaponShape shape) {

    /** Keeps the component object-shaped so existing stacks with a retired `grip` field still decode. */
    public static final Codec<WeaponClassComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeaponShape.CODEC.fieldOf("shape").forGetter(WeaponClassComponent::shape)
    ).apply(instance, WeaponClassComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponClassComponent> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public String translationKey() {
        return "tooltip.gene_hunter.weapon_class." + shape.id();
    }

    public enum WeaponShape {
        BLADE("blade"),
        SWORD("sword"),
        AXE("axe"),
        HAMMER("hammer");

        public static final Codec<WeaponShape> CODEC = Codec.STRING.xmap(WeaponShape::byId, WeaponShape::id);
        private final String id;

        WeaponShape(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        private static WeaponShape byId(String id) {
            for (WeaponShape shape : values()) {
                if (shape.id.equals(id)) {
                    return shape;
                }
            }
            return SWORD;
        }
    }
}
