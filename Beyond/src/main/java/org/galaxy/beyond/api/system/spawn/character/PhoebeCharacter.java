package org.galaxy.beyond.api.system.spawn.character;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;

public class PhoebeCharacter extends CassandraCharacter {
    public static final ResourceLocation ID = Beyond.asResource("phoebe");

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
