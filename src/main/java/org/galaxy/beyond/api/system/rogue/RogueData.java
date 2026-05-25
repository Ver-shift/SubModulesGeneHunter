package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.init.BeyondRogueCapInit;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class RogueData implements IPersistedSerializable {

    public static final MapCodec<RogueData> CODEC = PersistedParser.createMapCodec(RogueData::new);
    public static final StreamCodec<ByteBuf, RogueData> STREAM_CODEC = PersistedParser.createStreamCodec(RogueData::new);

    @Persisted
    private ResourceKey<Level> rogueLevel = Level.OVERWORLD;

    @Persisted
    private Identifier phaseId = RoguePhase.LOBBY.getId();

    @Persisted(subPersisted = true)
    private RogueNodeData rogueNodeData = new RogueNodeData();

    @Persisted(subPersisted = true)
    private ProgressType progressType = new ProgressType();

    @Persisted
    private long gameSeed;

    @Persisted
    @ReadOnlyManaged(serializeMethod = "rogueCapDataSerialize", deserializeMethod = "rogueCapDataDeserialize")
    private final List<RogueCapData> rogueCapData = new CopyOnWriteArrayList<>();
    /** 节点区域 → NodeData（发现时创建，带颜色，跨局持久） */
    @Persisted
    @ReadOnlyManaged(serializeMethod = "nodeDatasSerialize", deserializeMethod = "nodeDatasDeserialize")
    private final List<NodeData> nodeDatas = new CopyOnWriteArrayList<>();

    @Persisted
    private List<String> roguePlayerIdStrings = new ArrayList<>();

    @Persisted
    private List<String> safeZonePlayerIdStrings = new ArrayList<>();

    /** 按区块查找 NodeData */
    public NodeData findNodeData(ChunkPos pos) {
        for (var nd : nodeDatas)
            if (nd.containsChunk(pos)) return nd;
        return null;
    }

    /** 添加新 NodeData（去重） */
    public void addNodeData(NodeData nd) {
        ChunkPos key = nd.getNodeChunks().isEmpty() ? null : new ChunkPos(ChunkPos.getX(nd.getNodeChunks().getFirst()), ChunkPos.getZ(nd.getNodeChunks().getFirst()));
        if (key != null && findNodeData(key) == null) nodeDatas.add(nd);
    }

    // ---- 当前进度 ----

    public ProgressType getProgressType() {
        if (progressType == null) {
            progressType = new ProgressType();
        }
        return progressType;
    }

    public void setProgressType(ProgressType progressType) {
        this.progressType = progressType != null ? progressType : new ProgressType();
    }

    public Identifier getProgressId() {
        return getProgressType().getId();
    }

    public void setProgressId(Identifier progressId) {
        getProgressType().setId(progressId);
    }

    public boolean hasProgressId() {
        return getProgressId() != null;
    }

    public void setProgressActive(boolean active) {
        getProgressType().setActive(active);
    }

    // ---- 肉鸽玩家列表 ----

    public Set<UUID> getRoguePlayerIds() {
        return toStringSet(roguePlayerIdStrings);
    }

    public boolean addRoguePlayer(UUID playerId) {
        String s = playerId.toString();
        if (roguePlayerIdStrings.contains(s)) return false;
        safeZonePlayerIdStrings.remove(s);
        return roguePlayerIdStrings.add(s);
    }

    public boolean removeRoguePlayer(UUID playerId) {
        return roguePlayerIdStrings.remove(playerId.toString());
    }

    public boolean isRoguePlayer(UUID playerId) {
        return roguePlayerIdStrings.contains(playerId.toString());
    }

    // ---- 安全区玩家列表 ----

    public Set<UUID> getSafeZonePlayerIds() {
        return toStringSet(safeZonePlayerIdStrings);
    }

    public boolean addSafeZonePlayer(UUID playerId) {
        String s = playerId.toString();
        if (safeZonePlayerIdStrings.contains(s)) return false;
        roguePlayerIdStrings.remove(s);
        return safeZonePlayerIdStrings.add(s);
    }

    public boolean removeSafeZonePlayer(UUID playerId) {
        return safeZonePlayerIdStrings.remove(playerId.toString());
    }

    public boolean isSafeZonePlayer(UUID playerId) {
        return safeZonePlayerIdStrings.contains(playerId.toString());
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

    /** 首次加载时初始化全局默认 cap 列表，已存在则跳过。 */
    public void initDefaultCaps() {
        if (!rogueCapData.isEmpty()) return;
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.PROGRESS_START.get()));
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.PLAYER_IN_GAME.get()));
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.NODE_CAP.get()));
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.NODE_ZONE_ENTER.get()));
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.ROGUE_INIT.get()));
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.ROGUE_PROGRESS_FINISH.get()));
        rogueCapData.add(new RogueCapData(BeyondRogueCapInit.PLAYER_PROGRESS_FINISH.get()));
    }

    // ============================================================
    // 转换层 —— 外部直接用 RoguePhase，内部存 Identifier
    // ============================================================

    public RoguePhase getPhase() {
        return RoguePhase.byId(phaseId);
    }

    public void setPhase(RoguePhase phase) {
        this.phaseId = phase.getId();
    }

    // ---- 行为方法 ----

    /** 进度+1，返回 true 表示所有 scene 已完成 */
    public boolean advanceProgress() {
        return getProgressType().advanceScene();
    }

    /** 新一局开始时重置运行时状态 */
    public void resetProgressState() {
        setRogueNodeData(null);
        getProgressType().resetSceneIndex();
    }

    @SuppressWarnings("unused")
    private CompoundTag rogueCapDataSerialize(List<RogueCapData> list) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (RogueCapData cd : list) {
            RogueCapData.CODEC.codec().encodeStart(NbtOps.INSTANCE, cd)
                    .result().ifPresent(items::add);
        }
        tag.put("items", items);
        return tag;
    }

    @SuppressWarnings("unused")
    private List<RogueCapData> rogueCapDataDeserialize(CompoundTag tag) {
        List<RogueCapData> list = new CopyOnWriteArrayList<>();
        ListTag items = tag.getListOrEmpty("items");
        for (int i = 0; i < items.size(); i++) {
            RogueCapData.CODEC.codec().parse(NbtOps.INSTANCE, items.get(i))
                    .result().ifPresent(list::add);
        }
        return list;
    }

    @SuppressWarnings("unused")
    private CompoundTag nodeDatasSerialize(List<NodeData> list) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (NodeData nd : list) {
            NodeData.CODEC.codec().encodeStart(NbtOps.INSTANCE, nd)
                    .result().ifPresent(items::add);
        }
        tag.put("items", items);
        return tag;
    }

    @SuppressWarnings("unused")
    private List<NodeData> nodeDatasDeserialize(CompoundTag tag) {
        List<NodeData> list = new CopyOnWriteArrayList<>();
        ListTag items = tag.getListOrEmpty("items");
        for (int i = 0; i < items.size(); i++) {
            NodeData.CODEC.codec().parse(NbtOps.INSTANCE, items.get(i))
                    .result().ifPresent(list::add);
        }
        return list;
    }

}
