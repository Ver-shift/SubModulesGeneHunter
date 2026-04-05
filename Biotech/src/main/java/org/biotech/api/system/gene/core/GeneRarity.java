package org.biotech.api.system.gene.core;

import net.minecraft.world.item.Rarity;

public enum GeneRarity {
    UNCOMMON(1, Rarity.UNCOMMON),
    RARE(2, Rarity.RARE),
    EPIC(3, Rarity.EPIC);

    private final int value;
    private final Rarity minecraftRarity;

    GeneRarity(final int value, Rarity minecraftRarity) {
        this.value = value;
        this.minecraftRarity = minecraftRarity;
    }

    public int getValue() {
        return value;
    }

    public Rarity getMinecraftRarity() {
        return minecraftRarity;
    }

    public static Rarity toMinecraftRarity(GeneRarity rarity) {
        return rarity != null ? rarity.getMinecraftRarity() : Rarity.COMMON;
    }

    public static GeneRarity fromMinecraftRarity(Rarity rarity) {
        for (GeneRarity geneRarity : values()) {
            if (geneRarity.minecraftRarity == rarity) {
                return geneRarity;
            }
        }
        return UNCOMMON;
    }

    public static GeneRarity fromValue(int value) {
        for (GeneRarity rarity : values()) {
            if (rarity.value == value) {
                return rarity;
            }
        }
        return UNCOMMON;
    }
}
