package org.galaxy.gene_hunter.api.system.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WeaponClassComponent(WeaponGrip grip, WeaponShape shape) {

    public static final Codec<WeaponClassComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeaponGrip.CODEC.fieldOf("grip").forGetter(WeaponClassComponent::grip),
            WeaponShape.CODEC.fieldOf("shape").forGetter(WeaponClassComponent::shape)
    ).apply(instance, WeaponClassComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponClassComponent> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public String translationKey() {
        return "tooltip.gene_hunter.weapon_class." + grip.id() + "." + shape.id();
    }

    public enum WeaponGrip {
        ONE_HAND("one_hand"),
        TWO_HAND("two_hand"),
        POLEARM("polearm");

        public static final Codec<WeaponGrip> CODEC = Codec.STRING.xmap(WeaponGrip::byId, WeaponGrip::id);
        private final String id;

        WeaponGrip(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        private static WeaponGrip byId(String id) {
            for (WeaponGrip grip : values()) {
                if (grip.id.equals(id)) {
                    return grip;
                }
            }
            return ONE_HAND;
        }
    }

    public enum WeaponShape {
        BLADE("blade"),
        SWORD("sword"),
        HALBERD("halberd"),
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
