package org.biotech.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.DoubleValue UNCOMMON_GENE_TRAIT_ROLL_COUNT;
    private static final ModConfigSpec.DoubleValue RARE_GENE_TRAIT_ROLL_COUNT;
    private static final ModConfigSpec.DoubleValue EPIC_GENE_TRAIT_ROLL_COUNT;

    static {
        BUILDER.comment("Unidentified gene trait roll count multipliers by item rarity.").push("unidentified_gene_item");

        UNCOMMON_GENE_TRAIT_ROLL_COUNT = BUILDER
                .comment("Default matches previous hardcoded value.")
                .defineInRange("uncommon_gene_trait_roll_count", 1.1D, 0.0D, 100.0D);

        RARE_GENE_TRAIT_ROLL_COUNT = BUILDER
                .comment("Default matches previous hardcoded value.")
                .defineInRange("rare_gene_trait_roll_count", 1.5D, 0.0D, 100.0D);

        EPIC_GENE_TRAIT_ROLL_COUNT = BUILDER
                .comment("Default matches previous hardcoded value.")
                .defineInRange("epic_gene_trait_roll_count", 2.1D, 0.0D, 100.0D);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ServerConfig() {
    }

    public static float getUncommonGeneTraitRollCount() {
        return UNCOMMON_GENE_TRAIT_ROLL_COUNT.get().floatValue();
    }

    public static float getRareGeneTraitRollCount() {
        return RARE_GENE_TRAIT_ROLL_COUNT.get().floatValue();
    }

    public static float getEpicGeneTraitRollCount() {
        return EPIC_GENE_TRAIT_ROLL_COUNT.get().floatValue();
    }
}
