package org.galaxy.beyond.api.system.rogue.core;

public enum RogueState {
    LOBBY,
    PRE_ROGUE,
    ROGUE_INIT,
    ON_PROGRESS,

    PRE_NODE,
    PRE_EVENT,
    ON_EVENT,

    ROGUE_PROGRESS_FINISH,
    EMPTY;

    public static boolean isNodeEvent(RogueState state) {
        return switch (state) {
            case PRE_NODE, PRE_EVENT, ON_EVENT -> true;
            default -> false;
        };
    }

}
