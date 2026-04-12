package com.pz.beyond.api.system.zone;

import com.pz.beyond.api.system.rule.RuleData;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractZone {

    /**
     * 标识符
     */
    protected final ResourceLocation identifier;

    /**
     * 在地图上面的颜色
     */
    private int mapChunkColor = 0xFFFFE6FF;


    public AbstractZone(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }




}
