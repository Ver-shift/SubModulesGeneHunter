package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.init.BeyondRogueCapInit;
import org.galaxy.beyond.api.event.custom.RogueCapInitEvent;
import org.galaxy.beyond.api.plugin.BeyondPluginRunner;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.spawn.SpawnSessionData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class RogueData implements IPersistedSerializable {

    public static final MapCodec<RogueData> CODEC = PersistedParser.createMapCodec(RogueData::new);
    public static final StreamCodec<ByteBuf, RogueData> STREAM_CODEC = PersistedParser.createStreamCodec(RogueData::new);

    @Persisted
    private ResourceKey<Level> rogueLevel = Level.OVERWORLD;

    @Persisted
    private ResourceLocation phaseId = RoguePhase.LOBBY.getId();

    @Persisted(subPersisted = true)
    private RogueNodeData rogueNodeData = new RogueNodeData();

    @Persisted(subPersisted = true)
    private ProgressType progressType = new ProgressType();

    @Persisted
    private long gameSeed;

    @Persisted(subPersisted = true)
    private SpawnSessionData currentSpawn;

    @Persisted
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

    public ResourceLocation getProgressId() {
        return getProgressType().getId();
    }

    public void setProgressId(ResourceLocation progressId) {
        getProgressType().setId(progressId);
    }

    public boolean hasProgressId() {
        return getProgressId() != null;
    }

    public void setProgressActive(boolean active) {
        getProgressType().setActive(active);
    }

    public void initDefaultCaps(Level level) {
        if (!rogueCapData.isEmpty()) return;
        RogueCapInitEvent.Default event = RogueCapInitEvent.post(
                new RogueCapInitEvent.Default(this, BeyondPluginRunner.collectDefaultRogueCaps())
        );
        addCaps(event.getCapIds());

        ResourceLocation progressId = getProgressId();
        ProgressDefinition definition = BeyondAPI.getRogueDefinition(level).getProgress(progressId);
        if (definition == null) return;
        RogueCapInitEvent.Progress progressEvent = RogueCapInitEvent.post(
                new RogueCapInitEvent.Progress(
                        this,
                        progressId,
                        definition,
                        BeyondPluginRunner.collectProgressCaps(progressId, definition)
                )
        );
        addCaps(progressEvent.getCapIds());
    }

    private void addCaps(List<ResourceLocation> capIds) {
        for (ResourceLocation id : capIds) {
            BeyondRogueCapInit.getById(id)
                    .map(net.minecraft.core.Holder.Reference::value)
                    .ifPresentOrElse(cap -> rogueCapData.add(new RogueCapData(cap)), () -> {
                        throw new IllegalStateException("RogueCap is not registered: " + id);
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
        setCurrentSpawn(null);
        getProgressType().resetSceneIndex();
    }

}
