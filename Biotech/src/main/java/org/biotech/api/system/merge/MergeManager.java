package org.biotech.api.system.merge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.biotech.api.GeneData;
import org.biotech.api.system.gene.core.IUnidentifiedGeneItem;
import org.biotech.api.init.BiotechItemInit;
import org.biotech.api.system.merge.core.IMergeManager;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import org.biotech.item.GeneItem;

/**
 * 合并管理器实现 - 玩家身上的概率合并系统
 * 
 * 输出规则（根据输入词条数量）：
 * - Epic：必定双词条
 * - Rare：更高概率双词条
 * - Uncommon：极小概率双词条
 * - Common：不会有
 */
public class MergeManager implements IMergeManager {

    private final GeneData data;
    private final MergeData mergeData;

    public MergeManager(GeneData data) {
        this.data = data;
        this.mergeData = data.getMergeData();
        this.mergeData.setOnInputSlotsChanged(this::updateSlotData);
    }


    @Override
    public void updateSlotData() {
        var slots = mergeData.getInputSlots();
        int totalTraitCount = 0;
        int geneSlotCount = 0;
        int geneItemCount = 0;

        for (int i = 0; i < slots.getSlots(); i++) {
            ItemStack stack = slots.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            totalTraitCount += IStackTraitAccess.getTraitCount(stack);
            if (stack.getItem() instanceof GeneItem) {
                geneSlotCount++;
                geneItemCount += stack.getCount();
            }
        }

        float perGene = geneSlotCount == 0 ? 0f : (float) totalTraitCount / geneSlotCount;
        int outputCount = calculateOutputCount(totalTraitCount, geneItemCount);

        mergeData.setCachedTraitCount(totalTraitCount);
        mergeData.setCachedTraitCountPerGene(perGene);
        mergeData.setCachedOutputXeneCount(outputCount);
        mergeData.setCachedGeneItemCount(geneItemCount);
        mergeData.setSlotDataCacheReady(true);

        var outputSlots = mergeData.getOutputSlots();
        if (outputCount <= 0) {
            outputSlots.setStackInSlot(0, ItemStack.EMPTY);
            return;
        }

        Rarity rarity = resolveOutputRarity(perGene);
        ItemStack previewStack = new ItemStack(getRewardItemByRarity(rarity));
        previewStack.setCount(Math.min(outputCount, previewStack.getMaxStackSize()));
        outputSlots.setStackInSlot(0, previewStack);
    }

    @Override
    public int traitCount() {
        if (!mergeData.isSlotDataCacheReady()) {
            updateSlotData();
        }
        return mergeData.getCachedTraitCount();
    }

    @Override
    public float traitCountPerGene() {
        if (!mergeData.isSlotDataCacheReady()) {
            updateSlotData();
        }
        return mergeData.getCachedTraitCountPerGene();
    }

    @Override
    public int outputXeneCount(int traitCount) {
        if (!mergeData.isSlotDataCacheReady()) {
            updateSlotData();
        }
        if (traitCount == mergeData.getCachedTraitCount()) {
            return mergeData.getCachedOutputXeneCount();
        }
        return calculateOutputCount(traitCount, mergeData.getCachedGeneItemCount());
    }

    @Override
    public void merge() {
        ServerPlayer player = data.getPlayer();
        if (player == null) {
            return;
        }

        updateSlotData();

        int totalTraitCount = traitCount();
        if (totalTraitCount <= 0) {
            return;
        }

        int rawOutput = outputXeneCount(totalTraitCount);
        if (rawOutput <= 0) {
            return;
        }

        int availableGeneCount = mergeData.getCachedGeneItemCount();
        int maxByInputGene = availableGeneCount / 3;
        int actualOutput = Math.min(rawOutput, maxByInputGene);
        if (actualOutput <= 0) {
            return;
        }

        float averageTraitPerGene = traitCountPerGene();
        consumeGeneItems(actualOutput * 3);
        updateSlotData();

        Rarity rarity = resolveOutputRarity(averageTraitPerGene);
        var rewardItem = getRewardItemByRarity(rarity);
        if (!(rewardItem instanceof IUnidentifiedGeneItem unidentifiedGeneItem)) {
            return;
        }

        for (int i = 0; i < actualOutput; i++) {
            unidentifiedGeneItem.use(player, rarity);
        }

        // Play one confirmation ding with the same parameters as unidentified gene usage.
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.6F,
                IUnidentifiedGeneItem.getSoundPitchFromRarity(rarity)
        );
    }


    private void consumeGeneItems(int needToConsume) {
        if (needToConsume <= 0) {
            return;
        }

        var slots = mergeData.getInputSlots();
        int remaining = needToConsume;
        for (int i = 0; i < slots.getSlots() && remaining > 0; i++) {
            ItemStack stack = slots.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof GeneItem)) {
                continue;
            }

            int consume = Math.min(stack.getCount(), remaining);
            stack.shrink(consume);
            slots.setStackInSlot(i, stack);
            remaining -= consume;
        }
    }

    private static Rarity resolveOutputRarity(float traitCountPerGene) {
        if (traitCountPerGene >= 3f) {
            return Rarity.EPIC;
        }
        if (traitCountPerGene >= 2f) {
            return Rarity.RARE;
        }
        return Rarity.UNCOMMON;
    }

    private static int calculateOutputCount(int traitCount, int geneItemCount) {
        if (traitCount <= 0 || geneItemCount < 3) {
            return 0;
        }
        // One output needs both: 3 traits-worth progress and 3 input gene items.
        return Math.min(traitCount / 3, geneItemCount / 3);
    }

    private static net.minecraft.world.item.Item getRewardItemByRarity(Rarity rarity) {
        return switch (rarity) {
            case EPIC -> BiotechItemInit.EPIC_UNIDENTIFIED_GENE.get();
            case RARE -> BiotechItemInit.RARE_UNIDENTIFIED_GENE.get();
            default -> BiotechItemInit.UNCOMMON_UNIDENTIFIED_GENE.get();
        };

    }
}
