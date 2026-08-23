package org.biotech;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.biotech.api.init.*;
import org.slf4j.Logger;

@Mod(Biotech.MODID)
public class Biotech {

    public static final String MODID = "biotech";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Biotech(IEventBus modEventBus, ModContainer modContainer) {

        newRegistryInit(modEventBus);

        BiotechDataComponentInit.register(modEventBus);
        BiotechItemInit.register(modEventBus);
        BiotechCreativeTabInit.register(modEventBus);
        BiotechAttachInit.register(modEventBus);
        BiotechAttributeInit.register(modEventBus);
        BiotechMenuInit.register(modEventBus);
    }

    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID,path.toLowerCase());
    }

    /**
     * 创建新的自己的注册项
     * @param modEventBus
     */
    public void newRegistryInit(IEventBus modEventBus) {

        BiotechGeneInit.register(modEventBus);

        BiotechLootTypeInit.register(modEventBus);


    }










}
