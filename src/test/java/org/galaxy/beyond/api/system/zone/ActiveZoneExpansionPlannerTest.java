package org.galaxy.beyond.api.system.zone;

import java.util.Set;

public final class ActiveZoneExpansionPlannerTest {

    public static void main(String[] args) {
        ignoresCompletedNodesWhenCountingTargets();
        expandsFromMinimumRadiusUntilEnoughUncompletedNodesAreFound();
    }

    private static void ignoresCompletedNodesWhenCountingTargets() {
        var current = Set.of(new ActiveZoneExpansionPlanner.Chunk(0, 0));
        var allNodes = Set.of(
                new ActiveZoneExpansionPlanner.Chunk(0, 0),
                new ActiveZoneExpansionPlanner.Chunk(2, 0),
                new ActiveZoneExpansionPlanner.Chunk(4, 0),
                new ActiveZoneExpansionPlanner.Chunk(6, 0));
        var completed = Set.of(new ActiveZoneExpansionPlanner.Chunk(0, 0), new ActiveZoneExpansionPlanner.Chunk(2, 0));

        var result = ActiveZoneExpansionPlanner.plan(current, allNodes, completed, 1, 2, 10);

        assertEquals(6, result.radius(), "radius skips completed node and reaches two unfinished nodes");
        assertEquals(2, result.uncompletedNodeCount(), "only unfinished node components count");
        assertEquals(true, result.satisfied(), "target count satisfied");
    }

    private static void expandsFromMinimumRadiusUntilEnoughUncompletedNodesAreFound() {
        var current = Set.of(new ActiveZoneExpansionPlanner.Chunk(10, 10));
        var allNodes = Set.of(
                new ActiveZoneExpansionPlanner.Chunk(10, 10),
                new ActiveZoneExpansionPlanner.Chunk(13, 10),
                new ActiveZoneExpansionPlanner.Chunk(20, 10));
        var completed = Set.of(new ActiveZoneExpansionPlanner.Chunk(10, 10));

        var result = ActiveZoneExpansionPlanner.plan(current, allNodes, completed, 2, 1, 8);

        assertEquals(3, result.radius(), "radius starts at minimum and grows to first unfinished node");
        assertEquals(1, result.uncompletedNodeCount(), "one unfinished node found");
        assertEquals(true, result.bounds().contains(new ActiveZoneExpansionPlanner.Chunk(13, 10)), "bounds include discovered node");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }
}
