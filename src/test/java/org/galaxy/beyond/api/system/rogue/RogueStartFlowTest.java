package org.galaxy.beyond.api.system.rogue;

public final class RogueStartFlowTest {

    public static void main(String[] args) {
        waitsWhenPlayersAreNotReady();
        waitsWhenNoProgressIsSelected();
        waitsWhenSelectedProgressDoesNotExist();
        startsWhenPlayersAndProgressAreReady();
    }

    private static void waitsWhenPlayersAreNotReady() {
        assertEquals(
                RogueStartFlow.Decision.WAITING_FOR_PLAYERS,
                RogueStartFlow.decide(false, true, true),
                "not all players ready");
    }

    private static void waitsWhenNoProgressIsSelected() {
        assertEquals(
                RogueStartFlow.Decision.NO_PROGRESS_SELECTED,
                RogueStartFlow.decide(true, false, false),
                "no progress selected");
    }

    private static void waitsWhenSelectedProgressDoesNotExist() {
        assertEquals(
                RogueStartFlow.Decision.PROGRESS_NOT_FOUND,
                RogueStartFlow.decide(true, true, false),
                "progress not found");
    }

    private static void startsWhenPlayersAndProgressAreReady() {
        assertEquals(
                RogueStartFlow.Decision.START,
                RogueStartFlow.decide(true, true, true),
                "ready and configured");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }
}
