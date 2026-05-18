package org.galaxy.beyond.api.system.rogue;

import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

public final class NodeInteractionFlow {

    private NodeInteractionFlow() {}

    public static Decision decide(NodeState nodeState, PlayerRogueState clickingPlayerState,
                                  int readyAfterClick, int totalPlayers,
                                  int currentEventIndex, int totalEvents) {
        if (totalPlayers <= 0) {
            return new Decision(Action.NO_PLAYERS, nodeState, clickingPlayerState, null);
        }

        return switch (nodeState) {
            case LOCKED -> readyAfterClick >= totalPlayers
                    ? new Decision(Action.START_EVENT, NodeState.ON_EVENT, PlayerRogueState.PRE_NODE, PlayerRogueState.ON_EVENT)
                    : new Decision(Action.ENTER_PRE_NODE, NodeState.PRE_NODE, PlayerRogueState.PRE_NODE, null);
            case PRE_NODE -> readyAfterClick >= totalPlayers
                    ? new Decision(Action.START_EVENT, NodeState.ON_EVENT, PlayerRogueState.PRE_NODE, PlayerRogueState.ON_EVENT)
                    : new Decision(Action.WAIT_PRE_NODE, NodeState.PRE_NODE, PlayerRogueState.PRE_NODE, null);
            case PRE_EVENT -> readyAfterClick >= totalPlayers
                    ? new Decision(Action.START_EVENT, NodeState.ON_EVENT, PlayerRogueState.PRE_EVENT, PlayerRogueState.ON_EVENT)
                    : new Decision(Action.WAIT_PRE_EVENT, NodeState.PRE_EVENT, PlayerRogueState.PRE_EVENT, null);
            case ON_EVENT -> {
                if (totalEvents <= 0) {
                    yield new Decision(Action.NO_EVENTS, NodeState.ON_EVENT, clickingPlayerState, null);
                }
                if (currentEventIndex >= totalEvents - 1) {
                    yield new Decision(Action.UNLOCK_NODE, NodeState.UNLOCKED, clickingPlayerState, null);
                }
                yield new Decision(Action.ADVANCE_EVENT, NodeState.ON_EVENT, clickingPlayerState, PlayerRogueState.ON_EVENT);
            }
            case UNLOCKED -> new Decision(Action.IGNORE_UNLOCKED, NodeState.UNLOCKED, clickingPlayerState, null);
        };
    }

    public record Decision(Action action, NodeState nextNodeState,
                           PlayerRogueState clickingPlayerState,
                           PlayerRogueState allPlayerState) {
    }

    public enum Action {
        ENTER_PRE_NODE,
        WAIT_PRE_NODE,
        START_EVENT,
        WAIT_PRE_EVENT,
        ADVANCE_EVENT,
        UNLOCK_NODE,
        IGNORE_UNLOCKED,
        UNLOCKED,
        NO_PLAYERS,
        NO_EVENTS,
        NO_PROGRESS
    }
}
