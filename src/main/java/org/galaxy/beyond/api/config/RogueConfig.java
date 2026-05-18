package org.galaxy.beyond.api.config;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 运行时配置，存入 BeyondGlobalData 持久化。
 * 所有 nullable 字段表示"未覆盖"，取值时兜底到 CommonConfig。
 */
public class RogueConfig implements IPersistedSerializable, IRogueConfig {

    // ==== 配置文件可覆盖（nullable = 未覆盖，reset() 清除） ====

    @Persisted @Setter private Boolean debugMode;
    @Persisted @Setter private Integer safeZoneSize;
    @Persisted @Setter private Integer minNodeExpandCount;
    @Persisted @Setter private Integer minNodeExpandChunks;
    @Persisted @Setter private Integer maxNodeExpandRange;
    @Persisted @Setter private Identifier currentProgress;
    @Persisted @Setter private ResourceKey<Level> rogueDimension;

    // ==== 纯持久化（reset() 不清除） ====

    @Persisted private List<String> roguePlayerIdStrings = new ArrayList<>();
    @Persisted private List<String> safeZonePlayerIdStrings = new ArrayList<>();

    public static final MapCodec<RogueConfig> CODEC = PersistedParser.createMapCodec(RogueConfig::new);
    public static final StreamCodec<ByteBuf, RogueConfig> STREAM_CODEC = PersistedParser.createStreamCodec(RogueConfig::new);

    // ==== 配置文件可覆盖 getter ====

    @Override
    public boolean isDebugMode() {
        return debugMode != null ? debugMode : CommonConfig.DEBUG_MODE.get();
    }

    @Override
    public int getSafeZoneSize() {
        return safeZoneSize != null ? safeZoneSize : 16;
    }

    @Override
    public int getMinNodeExpandCount() {
        return minNodeExpandCount != null ? minNodeExpandCount : CommonConfig.ACTIVE_ZONE_MIN_NODES.get();
    }

    @Override
    public int getMinNodeExpandChunks() {
        return minNodeExpandChunks != null ? minNodeExpandChunks : CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
    }

    @Override
    public int getMaxNodeExpandRange() {
        return maxNodeExpandRange != null ? maxNodeExpandRange : CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get();
    }

    @Override
    public Identifier getCurrentProgress() {
        return currentProgress;
    }

    @Override
    public ResourceKey<Level> getRogueDimension() {
        return rogueDimension != null ? rogueDimension : CommonConfig.getRogueDimension();
    }

    // ==== 肉鸽玩家列表 ====

    @Override
    public Set<UUID> getRoguePlayerIds() {
        return toStringSet(roguePlayerIdStrings);
    }

    @Override
    public boolean addRoguePlayer(UUID playerId) {
        String s = playerId.toString();
        if (roguePlayerIdStrings.contains(s)) return false;
        safeZonePlayerIdStrings.remove(s);
        return roguePlayerIdStrings.add(s);
    }

    @Override
    public boolean removeRoguePlayer(UUID playerId) {
        return roguePlayerIdStrings.remove(playerId.toString());
    }

    @Override
    public boolean isRoguePlayer(UUID playerId) {
        return roguePlayerIdStrings.contains(playerId.toString());
    }

    // ==== 安全区玩家列表 ====

    @Override
    public Set<UUID> getSafeZonePlayerIds() {
        return toStringSet(safeZonePlayerIdStrings);
    }

    @Override
    public boolean addSafeZonePlayer(UUID playerId) {
        String s = playerId.toString();
        if (safeZonePlayerIdStrings.contains(s)) return false;
        roguePlayerIdStrings.remove(s);
        return safeZonePlayerIdStrings.add(s);
    }

    @Override
    public boolean removeSafeZonePlayer(UUID playerId) {
        return safeZonePlayerIdStrings.remove(playerId.toString());
    }

    @Override
    public boolean isSafeZonePlayer(UUID playerId) {
        return safeZonePlayerIdStrings.contains(playerId.toString());
    }

    // ==== reset ====

    @Override
    public void reset() {
        debugMode = null;
        safeZoneSize = null;
        minNodeExpandCount = null;
        minNodeExpandChunks = null;
        maxNodeExpandRange = null;
        rogueDimension = null;
    }

    private static Set<UUID> toStringSet(List<String> strings) {
        Set<UUID> set = new LinkedHashSet<>();
        for (String s : strings) {
            try {
                set.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return set;
    }

    // ==== 脏标记 ====

    private transient int lastSyncedHash;

    public boolean isDirty() {
        return computeHash() != lastSyncedHash;
    }

    public void markSynced() {
        lastSyncedHash = computeHash();
    }

    private int computeHash() {
        return Objects.hash(debugMode, safeZoneSize, minNodeExpandCount, minNodeExpandChunks,
                maxNodeExpandRange, currentProgress, rogueDimension,
                roguePlayerIdStrings, safeZonePlayerIdStrings);
    }
}
