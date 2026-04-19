package com.pz.beyond.api.system.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A village structure snapshot with one main bounds and optional component bounds.
 */
public class VillageStructureData {

    private final String uniqueKey;
    private final ResourceLocation structureId;
    private final StructureComponentData mainComponent;
    private final List<StructureComponentData> components;
    private final long refreshGameTime;

    public VillageStructureData(String uniqueKey,
                                ResourceLocation structureId,
                                StructureComponentData mainComponent,
                                List<StructureComponentData> components,
                                long refreshGameTime) {
        this.uniqueKey = uniqueKey;
        this.structureId = structureId;
        this.mainComponent = mainComponent;
        this.components = new ArrayList<>(components);
        this.refreshGameTime = refreshGameTime;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public ResourceLocation getStructureId() {
        return structureId;
    }

    public StructureComponentData getMainComponent() {
        return mainComponent;
    }

    public List<StructureComponentData> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public long getRefreshGameTime() {
        return refreshGameTime;
    }

    public BlockPos center() {
        return mainComponent.center();
    }
}

