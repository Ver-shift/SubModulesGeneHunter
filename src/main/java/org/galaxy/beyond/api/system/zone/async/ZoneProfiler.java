package org.galaxy.beyond.api.system.zone.async;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import org.galaxy.beyond.Beyond;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public final class ZoneProfiler {

    private static final AtomicBoolean SPARK_STARTED = new AtomicBoolean(false);
    private static final String SPARK_START_COMMAND = "spark profiler start --timeout 600";

    private ZoneProfiler() {
    }

    public static void startSparkForInitial(ServerLevel level, ZoneExpansionJobType type, long jobId) {
        startSparkForInitial(type, jobId, () -> isSparkAvailable(level), () -> runSparkStartCommand(level));
    }

    static boolean startSparkForInitial(ZoneExpansionJobType type, long jobId,
                                        BooleanSupplier sparkAvailable, Runnable sparkStarter) {
        if (type != ZoneExpansionJobType.INITIAL) return false;
        if (!sparkAvailable.getAsBoolean()) {
            Beyond.profileInfo("[Zone][SPARK_SKIPPED] jobId={}, reason=unavailable", jobId);
            return false;
        }
        if (!SPARK_STARTED.compareAndSet(false, true)) return false;

        try {
            sparkStarter.run();
            Beyond.profileInfo("[Zone][SPARK] jobId={}, command=/{}", jobId, SPARK_START_COMMAND);
            return true;
        } catch (Throwable throwable) {
            Beyond.profileInfo("[Zone][SPARK_FAILED] jobId={}, error={}", jobId, throwable.toString());
            return false;
        }
    }

    static void resetSparkStartForTest() {
        SPARK_STARTED.set(false);
    }

    private static boolean isSparkAvailable(ServerLevel level) {
        return ModList.get().isLoaded("spark") && hasSparkCommand(level);
    }

    private static boolean hasSparkCommand(ServerLevel level) {
        return level.getServer().getCommands().getDispatcher().getRoot().getChild("spark") != null;
    }

    private static void runSparkStartCommand(ServerLevel level) {
        level.getServer().getCommands().performPrefixedCommand(
                level.getServer().createCommandSourceStack().withSuppressedOutput(),
                SPARK_START_COMMAND
        );
    }

    public static long nanoTime() {
        return System.nanoTime();
    }

    public static double elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }

    public static void logRequest(ZoneExpansionJobType type, ZoneExpansionRequest request) {
        Beyond.profileInfo(
                "[Zone][PROFILE_REQUEST] jobId={}, type={}, seeds={}, safe={}, node={}, active={}, uncompleted={}, minConnections={}, minRadius={}, maxRadius={}",
                request.jobId(),
                type,
                request.seeds().size(),
                request.safe().size(),
                request.node().size(),
                request.active().size(),
                request.uncompleted().size(),
                request.minConnections(),
                request.minRadius(),
                request.maxRadius()
        );
    }

    public static void logStep(long jobId, String step, double millis, Object... args) {
        Beyond.profileInfo("[Zone][PROFILE] jobId={}, step={}, millis={}, data={}",
                jobId, step, formatMillis(millis), Arrays.toString(args));
    }

    public static void logExpansionPlan(long jobId, int targetChunks, int targetComponents, int needed,
                                        int nearestDistance, int furthestDistance, int largestComponent) {
        Beyond.profileInfo(
                "[Zone][EXPAND_PLAN] jobId={}, targetChunks={}, targetComponents={}, needed={}, nearestDistance={}, furthestDistance={}, largestComponent={}",
                jobId, targetChunks, targetComponents, needed, nearestDistance, furthestDistance, largestComponent
        );
    }

    public static void logExpansionResult(long jobId, boolean success, int radius, int reached, int needed,
                                          int targetComponents, int addedChunks) {
        Beyond.profileInfo(
                "[Zone][EXPAND_RESULT] jobId={}, success={}, radius={}, reached={}, needed={}, targetComponents={}, addedChunks={}",
                jobId, success, radius, reached, needed, targetComponents, addedChunks
        );
    }

    public static void logApply(long jobId, ZoneExpansionJobType type, int from, int to, int batchSize,
                                int availableSize, double filterMillis, double writeMillis,
                                double syncMillis, double totalMillis) {
        Beyond.profileInfo(
                "[Zone][PROFILE_APPLY] jobId={}, type={}, from={}, to={}, batch={}, available={}, filterMillis={}, writeMillis={}, syncMillis={}, totalMillis={}",
                jobId,
                type,
                from,
                to,
                batchSize,
                availableSize,
                formatMillis(filterMillis),
                formatMillis(writeMillis),
                formatMillis(syncMillis),
                formatMillis(totalMillis)
        );
    }

    public static void logExpansionFailure(PendingZoneApply apply) {
        Beyond.profileInfo(
                "[Zone][EXPAND_FAILED] jobId={}, type={}, addedChunks={}, reachedNodeZones={}, targetNodeZones={}, neededNodeZones={}, nearestTargetDistance={}, furthestReachedDistance={}, radius={}",
                apply.getJobId(),
                apply.getType(),
                apply.getTotal(),
                apply.getReachedNodeZones(),
                apply.getTargetNodeZones(),
                apply.getNeededNodeZones(),
                apply.getNearestTargetDistance(),
                apply.getFurthestReachedDistance(),
                apply.getRadius()
        );
    }

    private static String formatMillis(double millis) {
        return String.format(java.util.Locale.ROOT, "%.3f", millis);
    }
}
