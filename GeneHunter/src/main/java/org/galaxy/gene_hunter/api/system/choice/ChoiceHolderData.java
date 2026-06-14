package org.galaxy.gene_hunter.api.system.choice;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class ChoiceHolderData implements IPersistedSerializable {

    public static final int CHOICE_SLOT_COUNT = 9;

    @Persisted
    @DescSynced
    private ItemStackHandler choiceHolder = new ItemStackHandler(CHOICE_SLOT_COUNT);

    @Persisted
    @DescSynced
    private int choiceCount = 3;

    @Persisted
    @DescSynced
    private boolean canRefresh = false;

    @Persisted
    @DescSynced
    private int refreshTimes = 0;

    @Persisted
    @DescSynced
    private int refreshCost = 0;

    @Persisted
    @DescSynced
    private int stageIndex = 0;

    @Persisted
    @DescSynced
    private int stageCount = 0;

    private ResourceLocation currentLootTypeId;

    private NodeColor currentNodeColor = NodeColor.GREEN;

    private float currentFixedRolls = 0.0F;

    private final List<ChoiceStage> stages = new ArrayList<>();

    private transient ServerPlayer player;

    public static final Codec<ChoiceHolderData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("current_loot_type").forGetter(data -> Optional.ofNullable(data.currentLootTypeId))
            ).apply(instance, lootTypeId -> {
                ChoiceHolderData data = new ChoiceHolderData();
                lootTypeId.ifPresent(data::setCurrentLootTypeId);
                return data;
            })
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceHolderData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            data -> Optional.ofNullable(data.currentLootTypeId),
            lootTypeId -> {
                ChoiceHolderData data = new ChoiceHolderData();
                lootTypeId.ifPresent(data::setCurrentLootTypeId);
                return data;
            }
    );

    public ItemStackHandler getChoiceHolderHandler() {
        return choiceHolder;
    }

    public void setCurrentLootType(ILootType<?> lootType) {
        this.currentLootTypeId = GalaxyLibLootTypeInit.getLootTypeId(lootType);
    }

    public ILootType<?> getCurrentLootType() {
        return currentLootTypeId == null ? null : GalaxyLibLootTypeInit.getLootTypeById(currentLootTypeId);
    }

    public void startStages(List<ChoiceStage> stages) {
        this.stages.clear();
        this.stages.addAll(stages);
        this.stageIndex = 0;
        this.stageCount = stages.size();
    }

    public ChoiceStage currentStage() {
        return hasStage() ? stages.get(stageIndex) : null;
    }

    public boolean hasNextStage() {
        return stageIndex + 1 < stages.size();
    }

    public boolean nextStage() {
        if (!hasNextStage()) {
            return false;
        }
        stageIndex++;
        return true;
    }

    public void clearStages() {
        stages.clear();
        stageIndex = 0;
        stageCount = 0;
        currentLootTypeId = null;
        currentFixedRolls = 0.0F;
        refreshTimes = 0;
        refreshCost = 0;
    }

    private boolean hasStage() {
        return stageIndex >= 0 && stageIndex < stages.size();
    }

    public void clear() {
        for (int i = 0; i < choiceHolder.getSlots(); i++) {
            choiceHolder.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
        }
    }
}
