package org.galaxy.beyond.api.system.rogue;

import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

public final class NodeInteractionFlowTest {

    public static void main(String[] args) {
        lockedClickStartsEventWhenClickCompletesReadiness();
        lockedClickWaitsWhenOtherPlayersAreNotReady();
        preNodeWaitsUntilAllPlayersReady();
        preNodeStartsEventWhenAllPlayersReady();
        preEventWaitsUntilAllPlayersReady();
        preEventStartsEventWhenAllPlayersReady();
        onEventAdvancesToNextEventWhenMoreEventsRemain();
        onEventUnlocksWhenCurrentEventIsFinal();
    }

    private static void lockedClickStartsEventWhenClickCompletesReadiness() {
        var decision = NodeInteractionFlow.decide(
                NodeState.LOCKED, PlayerRogueState.ON_PROGRESS, 1, 1, 0, 2);

        assertEquals(NodeInteractionFlow.Action.START_EVENT, decision.action(), "locked click action");
        assertEquals(NodeState.ON_EVENT, decision.nextNodeState(), "locked click next node");
        assertEquals(PlayerRogueState.PRE_NODE, decision.clickingPlayerState(), "locked click player state");
        assertEquals(PlayerRogueState.ON_EVENT, decision.allPlayerState(), "locked click all-player state");
    }

    private static void lockedClickWaitsWhenOtherPlayersAreNotReady() {
        var decision = NodeInteractionFlow.decide(
                NodeState.LOCKED, PlayerRogueState.ON_PROGRESS, 1, 2, 0, 2);

        assertEquals(NodeInteractionFlow.Action.ENTER_PRE_NODE, decision.action(), "locked waiting action");
        assertEquals(NodeState.PRE_NODE, decision.nextNodeState(), "locked waiting node");
        assertEquals(PlayerRogueState.PRE_NODE, decision.clickingPlayerState(), "locked waiting player state");
    }

    private static void preNodeWaitsUntilAllPlayersReady() {
        var decision = NodeInteractionFlow.decide(
                NodeState.PRE_NODE, PlayerRogueState.ON_PROGRESS, 1, 2, 0, 2);

        assertEquals(NodeInteractionFlow.Action.WAIT_PRE_NODE, decision.action(), "pre-node waiting action");
        assertEquals(NodeState.PRE_NODE, decision.nextNodeState(), "pre-node waiting node");
        assertEquals(PlayerRogueState.PRE_NODE, decision.clickingPlayerState(), "pre-node click marks player ready");
    }

    private static void preNodeStartsEventWhenAllPlayersReady() {
        var decision = NodeInteractionFlow.decide(
                NodeState.PRE_NODE, PlayerRogueState.ON_PROGRESS, 2, 2, 0, 2);

        assertEquals(NodeInteractionFlow.Action.START_EVENT, decision.action(), "pre-node ready action");
        assertEquals(NodeState.ON_EVENT, decision.nextNodeState(), "pre-node ready node");
        assertEquals(PlayerRogueState.ON_EVENT, decision.allPlayerState(), "pre-node ready all-player state");
    }

    private static void preEventWaitsUntilAllPlayersReady() {
        var decision = NodeInteractionFlow.decide(
                NodeState.PRE_EVENT, PlayerRogueState.ON_EVENT, 1, 2, 1, 3);

        assertEquals(NodeInteractionFlow.Action.WAIT_PRE_EVENT, decision.action(), "pre-event waiting action");
        assertEquals(NodeState.PRE_EVENT, decision.nextNodeState(), "pre-event waiting node");
        assertEquals(PlayerRogueState.PRE_EVENT, decision.clickingPlayerState(), "pre-event click marks player ready");
    }

    private static void preEventStartsEventWhenAllPlayersReady() {
        var decision = NodeInteractionFlow.decide(
                NodeState.PRE_EVENT, PlayerRogueState.ON_EVENT, 2, 2, 1, 3);

        assertEquals(NodeInteractionFlow.Action.START_EVENT, decision.action(), "pre-event ready action");
        assertEquals(NodeState.ON_EVENT, decision.nextNodeState(), "pre-event ready node");
        assertEquals(PlayerRogueState.ON_EVENT, decision.allPlayerState(), "pre-event ready all-player state");
    }

    private static void onEventAdvancesToNextEventWhenMoreEventsRemain() {
        var decision = NodeInteractionFlow.decide(
                NodeState.ON_EVENT, PlayerRogueState.ON_EVENT, 0, 2, 0, 2);

        assertEquals(NodeInteractionFlow.Action.ADVANCE_EVENT, decision.action(), "on-event next action");
        assertEquals(NodeState.ON_EVENT, decision.nextNodeState(), "on-event next node");
        assertEquals(PlayerRogueState.ON_EVENT, decision.allPlayerState(), "on-event next all-player state");
    }

    private static void onEventUnlocksWhenCurrentEventIsFinal() {
        var decision = NodeInteractionFlow.decide(
                NodeState.ON_EVENT, PlayerRogueState.ON_EVENT, 0, 2, 1, 2);

        assertEquals(NodeInteractionFlow.Action.UNLOCK_NODE, decision.action(), "on-event final action");
        assertEquals(NodeState.UNLOCKED, decision.nextNodeState(), "on-event final node");
        assertEquals(null, decision.allPlayerState(), "on-event final all-player state decided by progress");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }
}
