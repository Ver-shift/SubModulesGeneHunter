package org.biotech.api.system.merge;

import net.minecraft.world.item.ItemStack;
import org.biotech.api.system.GeneData;
import org.biotech.api.system.merge.core.IMergeManager;

/**
 * Compatibility shell for the retired trait/xene merge mechanic.
 * The UI can still open old player data, but it produces no gameplay output.
 */
public final class MergeManager implements IMergeManager {
    private final MergeData data;

    public MergeManager(GeneData geneData) {
        this.data = geneData.getMergeData();
        this.data.setOnInputSlotsChanged(this::updateSlotData);
    }

    @Override
    public void updateSlotData() {
        data.setCachedTraitCount(0);
        data.setCachedTraitCountPerGene(0);
        data.setCachedOutputXeneCount(0);
        data.setCachedGeneItemCount(0);
        data.setSlotDataCacheReady(true);
        data.getOutputSlots().setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override public int traitCount() { return 0; }
    @Override public float traitCountPerGene() { return 0; }
    @Override public int outputXeneCount(int traitCount) { return 0; }
    @Override public void merge() { updateSlotData(); }
}
