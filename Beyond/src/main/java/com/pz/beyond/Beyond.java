package com.pz.beyond;

import com.mojang.logging.LogUtils;
import com.pz.beyond.api.init.BeyondAttachInit;

import com.pz.beyond.api.init.BeyondZoneRuleInit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Beyond.MODID)
public class Beyond
{
    public static final String MODID = "beyond";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Beyond(IEventBus modEventBus, ModContainer modContainer)
    {

        BeyondAttachInit.register(modEventBus);

        modEventBus.addListener(BeyondZoneRuleInit::registerRegistry);
        BeyondZoneRuleInit.register(modEventBus);
    }






    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
