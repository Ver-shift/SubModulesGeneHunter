package org.galaxy.beyond.api.system.rogue.core;

public final class Transitions {

    private Transitions() {}

    public static ITransition immediately() {
        return (level, ctx) -> true;
    }

    public static ITransition allPlayers(PlayerRogueState state) {
        return (level, ctx) -> ctx.allPlayersMatch(level, state);
    }

    public static ITransition anyPlayer(PlayerRogueState state) {
        return (level, ctx) -> ctx.anyPlayerMatch(level, state);
    }
}
