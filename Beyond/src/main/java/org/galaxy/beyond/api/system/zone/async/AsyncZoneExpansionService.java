package org.galaxy.beyond.api.system.zone.async;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncZoneExpansionService {

    private static final int APPLY_PER_TICK = 2500;
    private static final int PROGRESS_INTERVAL = 20;
    private static final String[] SPINNER = {"|", "/", "-", "\\"};

    private final PackedZoneExpander expander = new PackedZoneExpander();
    private final AtomicLong nextJobId = new AtomicLong(1L);
    private final ExecutorService executor = Executors.newFixedThreadPool(workerCount(), workerFactory());
    private final Map<ResourceKey<Level>, RunningJob> running = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, PendingZoneApply> pending = new ConcurrentHashMap<>();

    public long nextJobId() {
        return nextJobId.getAndIncrement();
    }

    public boolean submit(ServerLevel level, ZoneExpansionJobType type, ZoneExpansionRequest request) {
        ResourceKey<Level> key = level.dimension();
        if (running.containsKey(key) || pending.containsKey(key)) return false;
        CompletableFuture<ZoneExpansionResult> future = CompletableFuture.supplyAsync(() -> expander.expand(request), executor);
        running.put(key, new RunningJob(type, future));
        return true;
    }

    public void clear(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        running.remove(key);
        pending.remove(key);
    }

    public void tick(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        RunningJob job = running.get(key);
        if (job != null && job.future().isDone()) {
            running.remove(key);
            ZoneExpansionResult result = job.future().join();
            pending.put(key, new PendingZoneApply(job.type(), result));
        } else if (job != null) {
            job.tickProgress(level);
            return;
        }

        PendingZoneApply apply = pending.get(key);
        if (apply == null) return;
        apply.apply(level, APPLY_PER_TICK);
        if (!apply.isDone()) {
            tickApplyProgress(level, apply);
            return;
        }

        pending.remove(key);
        org.galaxy.beyond.Beyond.debugInfo(
                "[Zone][EXPAND_SUMMARY] type={}, addedChunks={}, discoveredNodeZones={}, newNodeZones={}, radius={}",
                apply.getType(),
                apply.getTotal(),
                apply.getDiscoveredNodeZones(),
                apply.getNewNodeZones(),
                apply.getRadius()
        );
        if (!apply.isSuccess()) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("commands.beyond.activezone.expand.failed"), false);
            return;
        }
        if (apply.getType() == ZoneExpansionJobType.NODE_UNLOCK) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    zoneExpandedMessage(apply), false);
        } else if (apply.getType() == ZoneExpansionJobType.INITIAL) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.active_zone_initial_expanded",
                            apply.getRadius(), apply.getDiscoveredNodeZones(), apply.getNewNodeZones()), false);
        } else if (apply.getType() == ZoneExpansionJobType.WORLD_SEED) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.item.world_seed.expanded",
                            apply.getRadius(), apply.getDiscoveredNodeZones(), apply.getNewNodeZones()), false);
        }
    }

    public boolean hasWork(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        return running.containsKey(key) || pending.containsKey(key);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private static int workerCount() {
        return Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    }

    private static ThreadFactory workerFactory() {
        AtomicLong counter = new AtomicLong(1L);
        return runnable -> {
            Thread thread = new Thread(runnable, "Beyond-ZoneWorker-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    private static Component zoneExpandedMessage(PendingZoneApply apply) {
        if (apply.getReachedNodeZones() <= 0) {
            if (apply.getRadius() <= 0) {
                return Component.translatable("beyond.node.zone_expanded_no_target");
            }
            return Component.translatable("beyond.node.zone_expanded_no_node", apply.getRadius());
        }
        return Component.translatable("beyond.node.zone_expanded",
                apply.getRadius(), apply.getDiscoveredNodeZones(), apply.getNewNodeZones());
    }

    private static void tickApplyProgress(ServerLevel level, PendingZoneApply apply) {
        int ticks = level.getServer().getTickCount();
        if (ticks % PROGRESS_INTERVAL != 0) return;
        showProgress(level, Component.translatable(
                progressApplyKey(apply.getType()),
                spinner(ticks),
                apply.getCursor(),
                apply.getTotal()
        ));
    }

    private static void showProgress(ServerLevel level, Component message) {
        for (var player : level.players()) {
            player.displayClientMessage(message, true);
        }
    }

    private static String spinner(int ticks) {
        return SPINNER[(ticks / PROGRESS_INTERVAL) % SPINNER.length];
    }

    private static String progressScanKey(ZoneExpansionJobType type) {
        return type == ZoneExpansionJobType.INITIAL
                ? "beyond.node.world_initializing_scan"
                : "beyond.node.zone_expanding_scan";
    }

    private static String progressApplyKey(ZoneExpansionJobType type) {
        return type == ZoneExpansionJobType.INITIAL
                ? "beyond.node.world_initializing_apply"
                : "beyond.node.zone_expanding_apply";
    }

    private record RunningJob(ZoneExpansionJobType type, CompletableFuture<ZoneExpansionResult> future) {

        private void tickProgress(ServerLevel level) {
            int ticks = level.getServer().getTickCount();
            if (ticks % PROGRESS_INTERVAL != 0) return;
            showProgress(level, Component.translatable(progressScanKey(type), spinner(ticks)));
        }
    }
}
