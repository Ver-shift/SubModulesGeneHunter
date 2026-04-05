package org.biotech.gene;

import net.minecraft.resources.ResourceLocation;
import org.biotech.Biotech;
import org.biotech.api.system.gene.GeneConfigBuilder;
import org.biotech.api.system.gene.core.GeneRarity;
import org.biotech.api.system.gene.core.IGene;

/**
 * 空基因 - 专门用于存储词条的容器基因
 * <p>
 * 此基因不执行任何基因逻辑（tick/onEquip 等均为空实现），
 * 仅作为 TraitComp 的载体，用于 GeneItem 存储词条。
 * <p>
 * 使用场景：
 * <ul>
 *   <li>GeneTraitLootType 抽奖获得的词条存储</li>
 *   <li>需要纯词条效果的 GeneItem</li>
 * </ul>
 */

public class EmptyGene implements IGene {
    
    @Override
    public ResourceLocation getID() {
        return Biotech.asResource("empty_gene");
    }

    @Override
    public GeneConfigBuilder getConfigBuilder() {
        return GeneConfigBuilder
                .builder()
                .rarity(GeneRarity.UNCOMMON);
    }
    

}
