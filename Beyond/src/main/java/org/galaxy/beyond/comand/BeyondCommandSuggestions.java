package org.galaxy.beyond.comand;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

import java.util.concurrent.CompletableFuture;

final class BeyondCommandSuggestions {

    static CompletableFuture<Suggestions> onlinePlayers(CommandSourceStack source, SuggestionsBuilder builder) {
        for (var player : source.getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getName().getString());
        }
        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> nodeTypes(SuggestionsBuilder builder) {
        builder.suggest("any");
        builder.suggest("unlocked");
        builder.suggest("locked");
        builder.suggest("outside");
        builder.suggest("inside");
        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> playerPhases(SuggestionsBuilder builder) {
        for (PlayerPhase phase : PlayerPhase.values()) {
            builder.suggest(phase.name());
        }
        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> currentProgress(SuggestionsBuilder builder) {
        var globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        if (globalData == null) return builder.buildFuture();
        var def = globalData.getRogueDefinition();
        if (def == null) return builder.buildFuture();
        def.getRogueProgress().keySet().forEach(k -> builder.suggest("\"" + k + "\""));
        return builder.buildFuture();
    }

    private BeyondCommandSuggestions() {
    }
}
