package com.pz.beyond.api.system.structure;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Map;

/**
 * 安全区内的结构数据，属于自己的数据
 */
public class SafeStructure {

    //标识符
    private String id;

    /**
     * {@link StructureTemplateManager#get(ResourceLocation)} 中的结构列表，按照关卡顺序排列
     */
    private List<ResourceLocation> levelStructures;
    private int currentLevelIndex = 0;

    //空间位置
    private StructureComponentData structureComponentData;

    public final ResourceLocation getTexture(){
        return Beyond.asResource("textures/structure/"+id+".png");
    }

    public final ResourceLocation getStructure(){
        return Beyond.asResource("structure/"+id +".nbt");
    }

    public record StructureComponentData(
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {

        public static StructureComponentData fromBoundingBox(BoundingBox box) {
            return new StructureComponentData(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        }

        public BoundingBox toBoundingBox() {
            return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public BlockPos center() {
            return new BlockPos((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        }
    }
}
