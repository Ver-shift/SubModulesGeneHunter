package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.init.BeyondRogueCapInit;
import org.galaxy.beyond.api.event.custom.RogueCapInitEvent;
import org.galaxy.beyond.api.plugin.BeyondPluginRunner;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.util.CompatUtil;

import java.util.List;
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

    public ProgressType getProgressType() {
        if (progressType == null) {
            progressType = new ProgressType(ProgressType.DEFAULT_ID);
        }
        return progressType;
    }

    public void setProgressType(ProgressType progressType) {
        this.progressType = progressType != null ? progressType : new ProgressType(ProgressType.DEFAULT_ID);
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

    public void initDefaultCaps() {
        if (!rogueCapData.isEmpty()) return;
        RogueCapInitEvent event = RogueCapInitEvent.post(
                new RogueCapInitEvent(this, BeyondPluginRunner.collectDefaultRogueCaps())
        );
        for (Identifier id : event.getCapIds()) {
            BeyondRogueCapInit.getById(id)
                    .map(net.minecraft.core.Holder.Reference::value)
                    .ifPresentOrElse(cap -> rogueCapData.add(new RogueCapData(cap)), () -> {
                        throw new IllegalStateException("Default RogueCap is not registered: " + id);
                    });
        }
    }

    public RoguePhase getPhase() {
        return RoguePhase.byId(phaseId);
    }

    public void setPhase(RoguePhase phase) {
        this.phaseId = phase.getId();
    }

    public boolean advanceProgress() {
        return getProgressType().advanceScene();
    }

    public void resetProgressState() {
        setRogueNodeData(null);
        getProgressType().resetSceneIndex();
    }

    @SuppressWarnings("unused")
    private CompoundTag rogueCapDataSerialize(List<RogueCapData> list) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (RogueCapData capData : list) {
            RogueCapData.CODEC.codec().encodeStart(NbtOps.INSTANCE, capData)
                    .result().ifPresent(items::add);
        }
        tag.put("items", items);
        return tag;
    }

    @SuppressWarnings("unused")
    private List<RogueCapData> rogueCapDataDeserialize(CompoundTag tag) {
        List<RogueCapData> list = new CopyOnWriteArrayList<>();
        ListTag items = CompatUtil.getCompoundListOrEmpty(tag, "items");
        for (int i = 0; i < items.size(); i++) {
            RogueCapData.CODEC.codec().parse(NbtOps.INSTANCE, items.get(i))
                    .result().ifPresent(list::add);
        }
        return list;
    }

}
