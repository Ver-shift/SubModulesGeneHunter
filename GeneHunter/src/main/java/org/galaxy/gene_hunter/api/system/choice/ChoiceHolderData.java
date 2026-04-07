package org.galaxy.gene_hunter.api.system.choice;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootTableManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.LootPoolData;

import java.util.ArrayList;
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
    private ItemStackHandler choiceHolder = new ItemStackHandler(CHOICE_SLOT_COUNT);

    /**
     * 默认的数量
     */
    @Persisted
    @DescSynced
    private int choiceCount = 3;

    @Persisted
    @DescSynced
    private boolean canRefresh = false;

    /**
     * 当前抽取结果（包含物品列表和战利品类型）
     */
    private ILootTableManager.LootResult currentLootResult;

    private transient ServerPlayer player;

    public ChoiceHolderData() {

    }


    private static ItemStackHandler createChoiceHolderHandler() {
        return new ItemStackHandler(CHOICE_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
            }
        };
    }





    // CODEC - 序列化 currentLootResult
    public static final Codec<ChoiceHolderData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ILootTableManager.LootResult.CODEC.optionalFieldOf("current_loot_result").forGetter(data -> Optional.ofNullable(data.currentLootResult))
        ).apply(instance, lootResultOpt -> {
            ChoiceHolderData data = new ChoiceHolderData();
            lootResultOpt.ifPresent(data::setCurrentLootResult);
            return data;
        })
    );

    // STREAM_CODEC - 序列化 currentLootResult
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceHolderData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ILootTableManager.LootResult.STREAM_CODEC),
        data -> Optional.ofNullable(data.currentLootResult),
        lootResultOpt -> {
            ChoiceHolderData data = new ChoiceHolderData();
            lootResultOpt.ifPresent(data::setCurrentLootResult);
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
