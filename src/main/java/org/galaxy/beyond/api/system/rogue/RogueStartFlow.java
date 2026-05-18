package org.galaxy.beyond.api.system.rogue;

public final class RogueStartFlow {

    private RogueStartFlow() {}

    public static Decision decide(boolean allPlayersReady, boolean progressSelected, boolean progressExists) {
        if (!allPlayersReady) return Decision.WAITING_FOR_PLAYERS;
        if (!progressSelected) return Decision.NO_PROGRESS_SELECTED;
        if (!progressExists) return Decision.PROGRESS_NOT_FOUND;
        return Decision.START;
    }

    public enum Decision {
        WAITING_FOR_PLAYERS,
        NO_PROGRESS_SELECTED,
        PROGRESS_NOT_FOUND,
        START
    }
}
