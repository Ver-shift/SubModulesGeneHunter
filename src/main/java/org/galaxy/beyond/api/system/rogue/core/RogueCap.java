package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;

public abstract class RogueCap implements IRogueCap {

    public static final RogueCap EMPTY = new RogueCap(Beyond.asResource("")){};

    private final Identifier id;

    public RogueCap(Identifier id) {
        this.id = id;
    }

    public final Identifier getId() {
        return id;
    }

    public Component getDescription() {
        return null;
    }
}
