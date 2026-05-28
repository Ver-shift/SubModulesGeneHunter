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

    public void tick(ServerLevel level) {
        ResourceKey<Level> key = level.dimension();
        RunningJob job = running.get(key);
        if (job != null && job.future().isDone()) {
            running.remove(key);
            ZoneExpansionResult result = job.future().join();
            pending.put(key, new PendingZoneApply(job.type(), result));
        }

        PendingZoneApply apply = pending.get(key);
        if (apply == null) return;
        apply.apply(level, APPLY_PER_TICK);
        if (!apply.isDone()) return;

        pending.remove(key);
        if (!apply.isSuccess()) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("commands.beyond.activezone.expand.failed"), false);
            return;
        }
        if (apply.getType() == ZoneExpansionJobType.NODE_UNLOCK) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.zone_expanded"), false);
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

    private record RunningJob(ZoneExpansionJobType type, CompletableFuture<ZoneExpansionResult> future) {
    }
}
