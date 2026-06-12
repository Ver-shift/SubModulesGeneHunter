package org.galaxy.beyond.api.system.spawn.character;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;

public class RandyCharacter extends CassandraCharacter {
    public static final ResourceLocation ID = Beyond.asResource("randy");

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
