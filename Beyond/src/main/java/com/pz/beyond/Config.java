package com.pz.beyond;

import net.neoforged.neoforge.common.ModConfigSpec;


public class Config
{
    private static final ModConfigSpec.Builder BUILDER;

    public static final ModConfigSpec SPEC;


    static {
        BUILDER = new ModConfigSpec.Builder();

        SPEC = BUILDER.build();
    }

}
