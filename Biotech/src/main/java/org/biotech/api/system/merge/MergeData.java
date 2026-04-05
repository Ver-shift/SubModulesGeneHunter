package org.biotech.api.system.merge;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 合并数据 - 包含输入槽和输出槽
 */
@Data
public class MergeData implements IPersistedSerializable {

    // 槽位常量
    public static final int INPUT_SLOT_COUNT = 9;   // 输入槽数量
    public static final int OUTPUT_SLOT_COUNT = 1;  // 输出槽数量

    // Codec 和 StreamCodec
    public static final Codec<MergeData> CODEC = PersistedParser.createCodec(MergeData::new);
    public static final StreamCodec<ByteBuf, MergeData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    // 输入槽
    @Persisted
    private ItemStackHandler inputSlots;

    // 输出槽
    @Persisted
    private ItemStackHandler outputSlots;

    // runtime cache: updateSlotData() 统一刷新，减少频繁遍历输入槽
    @Persisted
    private boolean slotDataCacheReady;
    @Persisted
    private int cachedTraitCount;
    @Persisted
    private float cachedTraitCountPerGene;
    @Persisted
    private int cachedOutputXeneCount;
    @Persisted
    private int cachedGeneItemCount;

    // 输入槽变化监听（运行时）
    private transient Runnable onInputSlotsChanged;

    

    public MergeData() {
        this.inputSlots = createInputHandler();
        this.outputSlots = createOutputHandler();
        this.slotDataCacheReady = false;
    }

    public ItemStackHandler getInputSlots() {
        normalizeInputSlotsSize();
        return inputSlots;
    }

    private void normalizeInputSlotsSize() {
        if (inputSlots == null) {
            inputSlots = createInputHandler();
            return;
        }
        if (inputSlots.getSlots() == INPUT_SLOT_COUNT) {
            return;
        }

        ItemStackHandler resized = createInputHandler();
        int copyCount = Math.min(inputSlots.getSlots(), INPUT_SLOT_COUNT);
        for (int i = 0; i < copyCount; i++) {
            resized.setStackInSlot(i, inputSlots.getStackInSlot(i));
        }
        inputSlots = resized;
        slotDataCacheReady = false;
        if (onInputSlotsChanged != null) {
            onInputSlotsChanged.run();
        }
    }

    private ItemStackHandler createInputHandler() {
        return new ItemStackHandler(INPUT_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                slotDataCacheReady = false;
                if (onInputSlotsChanged != null) {
                    onInputSlotsChanged.run();
                }
            }
        };
    }

    private static ItemStackHandler createOutputHandler() {
        return new ItemStackHandler(OUTPUT_SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                // 输出槽仅作预览与按钮点击，不允许玩家直接拿取。
                return ItemStack.EMPTY;
            }
        };
    }
}
