package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

/** Applies Xene experience-gain multipliers to positive XP gains. */
@EventBusSubscriber(modid = GeneHunter.MODID)
public final class ExperienceGainHandler {
    private ExperienceGainHandler() {
    }

    @SubscribeEvent
    public static void onExperienceGain(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) {
            return;
        }

        var attribute = player.getAttribute(GeneHunterAttributeInit.EXPERIENCE_GAIN_MULTIPLIER.holder());
        double multiplier = attribute == null ? 1.0D : attribute.getValue();
        event.setAmount((int) Math.round(event.getAmount() * multiplier));
    }
}
