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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.List;
import java.util.Optional;

@Data
public class ChoiceHolderData implements IPersistedSerializable {

    // 槽位数量常量
    public static final int CHOICE_SLOT_COUNT = 9;

    /**
     * 用来进行暂时性的存储，用来显示存储信息
     */

    @Persisted
    @DescSynced
    private ItemStackHandler choiceHolder;

    /**
     * 默认的数量
     */
    @Persisted
    @DescSynced
    private int choiceCount = 3;

    private ILootType<?> currentLootType;

    private transient ServerPlayer player;

    public ChoiceHolderData() {
        this.choiceHolder = createChoiceHolderHandler();
    }

    public ChoiceHolderData(ItemStackHandler choiceHolder, int choiceCount) {
        this.choiceHolder = choiceHolder != null ? choiceHolder : createChoiceHolderHandler();
        this.choiceCount = choiceCount;
    }

    private static ItemStackHandler createChoiceHolderHandler() {
        return new ItemStackHandler(CHOICE_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
            }
        };
    }

    private Optional<ResourceLocation> getCurrentLootTypeId() {
        return Optional.ofNullable(currentLootType)
            .map(type -> GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY.getKey(type));
    }

    private static ILootType<?> resolveLootType(Optional<ResourceLocation> lootTypeId) {
        return lootTypeId.map(GalaxyLibLootTypeInit::getLootTypeById).orElse(null);
    }

    // CODEC - choiceHolder/choiceCount 交给注解持久化，这里补充手写兼容字段（lootType）
    public static final Codec<ChoiceHolderData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("choice_count").forGetter(ChoiceHolderData::getChoiceCount),
            ResourceLocation.CODEC.optionalFieldOf("loot_type_id").forGetter(ChoiceHolderData::getCurrentLootTypeId)
        ).apply(instance, (count, lootTypeId) -> {
            ChoiceHolderData data = new ChoiceHolderData(null, count);
            data.setCurrentLootType(resolveLootType(lootTypeId));
            return data;
        })
    );

    // STREAM_CODEC - 与 CODEC 保持一致，网络同步 choiceCount + lootTypeId
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceHolderData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ChoiceHolderData::getChoiceCount,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        ChoiceHolderData::getCurrentLootTypeId,
        (count, lootTypeId) -> {
            ChoiceHolderData data = new ChoiceHolderData(null, count);
            data.setCurrentLootType(resolveLootType(lootTypeId));
            return data;
        }
    );




    public ItemStackHandler getChoiceHolderHandler() {
        return choiceHolder;
    }


    /**
     * 清空选择槽
     */
    public void clear() {
        for (int i = 0; i < choiceHolder.getSlots(); i++) {
            choiceHolder.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

}
