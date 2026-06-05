package org.galaxy.beyond.api.system.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.Beyond;

public final class BeyondAdvancements {

    public static final String WELCOME_MY_ROGUE = "welcome_my_rogue";
    public static final String ENTERED = "entered";

    private BeyondAdvancements() {
    }

    public static void grantWelcome(ServerPlayer player) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Beyond.asResource(WELCOME_MY_ROGUE));
        if (advancement == null) return;

        player.getAdvancements().award(advancement, ENTERED);
    }
}
