package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;

public abstract class RogueCap implements IRogueCap {

    public static final RogueCap EMPTY = new RogueCap(Beyond.asResource("")){};

    private final ResourceLocation id;

    public RogueCap(ResourceLocation id) {
        this.id = id;
    }

    public final ResourceLocation getId() {
        return id;
    }

    public Component getDescription() {
        return null;
    }
}
