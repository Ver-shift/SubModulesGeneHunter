package org.galaxy.beyond.api.init;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.pack.ProgressDataPack;

public class BeyondPackInit {

    /**
     * 注册 mod 内置数据包 —— Mod 总线（AddPackFindersEvent 是 IModBusEvent）
     */
    @EventBusSubscriber(modid = Beyond.MODID)
    public static class ModBus {
        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event) {
            event.addPackFinders(
                    Identifier.fromNamespaceAndPath(Beyond.MODID, "beyond_data"),
                    PackType.SERVER_DATA,
                    Component.literal("Beyond 内置数据"),
                    PackSource.BUILT_IN,
                    true,   // alwaysActive — 始终启用，玩家无法关闭
                    Pack.Position.TOP
            );
        }
    }

    /**
     * 注册重载监听器 —— NeoForge 总线（AddServerReloadListenersEvent 是普通事件）
     */
    @EventBusSubscriber(modid = Beyond.MODID)
    public static class NeoBus {
        private static final ProgressDataPack ROGUE_DATA = new ProgressDataPack();

        @SubscribeEvent
        public static void onAddReloadListeners(AddServerReloadListenersEvent event) {

        }
    }
}
