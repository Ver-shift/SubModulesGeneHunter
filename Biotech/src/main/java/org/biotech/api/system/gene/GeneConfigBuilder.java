package org.biotech.api.system.gene;


import lombok.Getter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import org.biotech.api.system.gene.core.GeneRarity;
import org.biotech.api.init.BiotechDataComponentInit;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;

/**
 * 通过数据组件进行控制
 */
@Getter
public class GeneConfigBuilder {

    private final DataComponentMap.Builder componentBuilder = DataComponentMap.builder();


    public GeneConfigBuilder() {
    }

    public static GeneConfigBuilder builder() {
        return new GeneConfigBuilder();
    }

    /**
     * 设置基因稀有度
     * @param rarity
     * @return
     */
    public GeneConfigBuilder rarity(GeneRarity rarity){
        componentBuilder.set(DataComponents.RARITY, rarity.getMinecraftRarity());
        return this;
    }


    private TraitComp traitCompBuilder = null;

    /**
     * 添加词条 - 支持多次调用累加
     * @param trait
     * @return
     */
    public GeneConfigBuilder trait(ITrait trait){
        if (traitCompBuilder == null) {
            traitCompBuilder = new TraitComp();
        }
        traitCompBuilder.addTrait(trait);
        return this;
    }

    /**
     * 设置多个词条（覆盖已有）
     * @param traits
     * @return
     */
    public GeneConfigBuilder traits(ITrait... traits){
        traitCompBuilder = TraitComp.of(traits);
        return this;
    }


    /**
     * 设置基因组件
     * @param componentType
     * @param value
     * @return
     * @param <T>
     */
    public <T> GeneConfigBuilder set(DataComponentType<? super T> componentType, T value) {
        componentBuilder.set(componentType, value);
        return this;
    }

    /**
     * 完成构建
     * @return
     */
    public DataComponentMap build() {
        // 将累积的词条组件设置到 builder
        if (traitCompBuilder != null && !traitCompBuilder.isEmpty()) {
            componentBuilder.set(BiotechDataComponentInit.TRAIT_COMP.get(), traitCompBuilder);
        }

        if (!valid()) {
            throw new IllegalStateException("Invalid gene config");
        }
        return componentBuilder.build();
    }

    private boolean valid(){
        // 检查必需组件是否已设置
        DataComponentMap map = componentBuilder.build();

        // 稀有度不能为空
        if (!map.has(DataComponents.RARITY)) {
            throw new IllegalStateException("Gene rarity must be set");
        }

        return true;
    }



}
