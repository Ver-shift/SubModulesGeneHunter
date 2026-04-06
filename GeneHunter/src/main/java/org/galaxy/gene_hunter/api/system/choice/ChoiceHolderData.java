package org.galaxy.gene_hunter.api.system.choice;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChoiceHolderData {

    // 槽位数量常量
    public static final int CHOICE_SLOT_COUNT = 9;

    /**
     * 用来进行暂时性的存储，用来显示存储信息
     */
    private ItemStackHandler choiceHolder;

    /**
     * 默认的数量
     */
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

    private ItemStackHandler createChoiceHolderHandler() {
        return new ItemStackHandler(CHOICE_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
            }
        };
    }

    // CODEC - 序列化（手动编写，因为 ILootType 无法自动处理）
    public static final Codec<ChoiceHolderData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemStack.CODEC.listOf().fieldOf("choice_holder").forGetter(ChoiceHolderData::getChoiceHolderAsList),
            Codec.INT.fieldOf("choice_count").forGetter(ChoiceHolderData::getChoiceCount)
        ).apply(instance, (items, count) -> {
            ItemStackHandler handler = new ItemStackHandler(CHOICE_SLOT_COUNT);
            for (int i = 0; i < handler.getSlots() && i < items.size(); i++) {
                handler.setStackInSlot(i, items.get(i));
            }
            return new ChoiceHolderData(handler, count);
        })
    );

    // STREAM_CODEC - 网络同步
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceHolderData> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)),
        ChoiceHolderData::getChoiceHolderAsList,
        ByteBufCodecs.VAR_INT,
        ChoiceHolderData::getChoiceCount,
        (items, count) -> {
            ItemStackHandler handler = new ItemStackHandler(CHOICE_SLOT_COUNT);
            for (int i = 0; i < handler.getSlots() && i < items.size(); i++) {
                handler.setStackInSlot(i, items.get(i));
            }
            return new ChoiceHolderData(handler, count);
        }
    );

    /**
     * 获取选择槽中的物品列表（用于序列化）
     */
    private List<ItemStack> getChoiceHolderAsList() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < choiceHolder.getSlots(); i++) {
            list.add(choiceHolder.getStackInSlot(i));
        }
        return list;
    }

    /**
     * 获取选择槽中的物品列表（兼容旧代码）
     */
    public List<ItemStack> getChoiceHolder() {
        return getChoiceHolderAsList();
    }

    /**
     * 设置选择槽中的物品（兼容旧代码）
     */
    public void setChoiceHolder(List<ItemStack> items) {
        for (int i = 0; i < choiceHolder.getSlots() && i < items.size(); i++) {
            choiceHolder.setStackInSlot(i, items.get(i));
        }
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
