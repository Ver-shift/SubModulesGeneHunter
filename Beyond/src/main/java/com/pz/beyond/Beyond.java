package com.pz.beyond;

import com.mojang.logging.LogUtils;
import com.pz.beyond.api.config.ServerConfig;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.init.BeyondEncounters;
import com.pz.beyond.api.init.BeyondNodeEventTypes;
import com.pz.beyond.api.init.BeyondZoneInit;

import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.pz.beyond.api.system.BeyondManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Beyond.MODID)
public class Beyond
{
    public static final String MODID = "beyond";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static BeyondManager BEYOND_MANAGER;


    public Beyond(IEventBus modEventBus, ModContainer modContainer)
    {

        BeyondAttachInit.register(modEventBus);

        newRegister(modEventBus);

//        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        BEYOND_MANAGER = new BeyondManager();

    }

    public static void newRegister(IEventBus modEventBus) {
        modEventBus.addListener(BeyondEncounters::registerRegistry);
        BeyondEncounters.register(modEventBus);

        modEventBus.addListener(BeyondNodeEventTypes::registerRegistry);
        BeyondNodeEventTypes.register(modEventBus);

        modEventBus.addListener(BeyondZoneRuleInit::registerRegistry);
        BeyondZoneRuleInit.register(modEventBus);

        modEventBus.addListener(BeyondZoneInit::registerRegistry);
        BeyondZoneInit.register(modEventBus);
    }




    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }
}
