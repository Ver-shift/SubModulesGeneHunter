package org.biotech;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.biotech.api.config.ServerConfig;
import org.biotech.api.event.handle.PlayerTraitHandle;
import org.biotech.api.init.*;
import org.biotech.api.init.MenuInit;
import org.slf4j.Logger;

@Mod(Biotech.MODID)
public class Biotech {

    public static final String MODID = "biotech";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Biotech(IEventBus modEventBus, ModContainer modContainer) {

        newRegistryInit(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);


        DataComponentInit.register(modEventBus);
        ItemInit.register(modEventBus);
        CreativeTabInit.register(modEventBus);
        AttachInit.register(modEventBus);
        AttributeInit.register(modEventBus);
        MenuInit.register(modEventBus);

        // 显式注册 Curios 属性事件，避免注解订阅失效时不触发。
        NeoForge.EVENT_BUS.addListener(PlayerTraitHandle::onCurioAttributeModifier);

    }

    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID,path.toLowerCase());
    }

    /**
     * 创建新的自己的注册项
     * @param modEventBus
     */
    public void newRegistryInit(IEventBus modEventBus) {

        modEventBus.addListener(TraitInit::registerRegistry);
        TraitInit.register(modEventBus);
        TraitInit.autoRegisterTraits();

        modEventBus.addListener(GeneInit::registerRegistry);
        GeneInit.register(modEventBus);
        GeneInit.autoRegisterGenes();

        modEventBus.addListener(LootTypeInit::registerRegistry);
        LootTypeInit.register(modEventBus);
    }










}
