package org.biotech.api.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.biotech.Biotech;
import org.biotech.api.system.gene.GeneConfigBuilder;
import org.biotech.api.system.gene.core.IGene;
import org.biotech.api.util.ModPluginFinder;
import org.biotech.gene.EmptyGene;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BiotechGeneInit {
    public static final ResourceKey<Registry<IGene>> GENE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Biotech.asResource("gene_registry"));
    public static final Registry<IGene> GENE_REGISTRY = new RegistryBuilder<>(GENE_REGISTRY_KEY).create();
    public static final DeferredRegister<IGene> GENE =
            DeferredRegister.create(GENE_REGISTRY_KEY, Biotech.MODID);
    public static void registerRegistry(NewRegistryEvent event) {
        event.register(GENE_REGISTRY);
    }
    public static void register(IEventBus eventBus) {
        GENE.register(eventBus);
    }


    public static final IGene EMPTY = new IGene() {
        @Override
        public ResourceLocation getID() {
            return null;
        }

        @Override
        public GeneConfigBuilder getConfigBuilder() {
            return null;
        }
    };


    /**
     * 获取所有代码基因（Registry 中的）
     */
    public static List<IGene> getAllCodeGenes() {
        return GENE_REGISTRY.stream().collect(Collectors.toList());
    }

    /**
     * 获取所有基因（代码基因 + 数据包基因）
     */
    public static List<IGene> getAllGenes() {
        List<IGene> allGenes = new ArrayList<>();
        allGenes.addAll(GENE_REGISTRY.stream().collect(Collectors.toList()));
        return allGenes;
    }

    public static Supplier<IGene> registerGene(Supplier<? extends IGene> sup) {
        return GENE.register(sup.get().getID().getPath(), sup);
    }

    /**
     * 通过 @AutoInit(type = GENE) 注解自动发现并注册基因
     * 扫描所有带有该注解的 IGene 实现类并注册
     */
    public static void autoRegisterGenes() {
        // 扫描 @AutoInit(type = GENE) 注解的类
        List<IGene> autoGenes = ModPluginFinder.getModPlugins();
        for (IGene gene : autoGenes) {
            String path = gene.getID().getPath();
            GENE.register(path, () -> gene);
            Biotech.LOGGER.info("Auto-registered gene: {}", gene.getID());
        }
    }

    public static final Supplier<IGene> EMPTY_GENE;


    static {
        EMPTY_GENE = GENE.register("empty_gene", EmptyGene::new);
    }






    /**
     * 修改后的 getGeneById，同时搜索代码基因和数据包基因
     */
    public static IGene getGeneById(ResourceLocation geneId) {
        // 先搜索代码基因
        var gene = GENE_REGISTRY.get(geneId);
        if (gene != null) {
            return gene;
        }
        return BiotechGeneInit.EMPTY;
    }
}
