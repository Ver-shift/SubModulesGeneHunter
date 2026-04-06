package org.galaxylib;


import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxylib.api.init.GalaxyLibAttachInit;
import org.galaxylib.api.init.GalaxyLibAttributeInit;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.slf4j.Logger;

@Mod(GalaxyLib.MODID)
public class GalaxyLib {
    public static final String MODID = "galaxylib";
    public static final Logger LOGGER = LogUtils.getLogger();

     public GalaxyLib(IEventBus modEventBus, ModContainer modContainer) {
         GalaxyLibAttributeInit.register(modEventBus);

         GalaxyLibLootTypeInit.register(modEventBus);
         modEventBus.addListener(GalaxyLibLootTypeInit::registerRegistry);

         GalaxyLibAttachInit.register(modEventBus);
     }


     public static ResourceLocation asResource(String path) {
         return ResourceLocation.fromNamespaceAndPath(MODID,path.toLowerCase());
     }
}
