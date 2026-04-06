package org.galaxy.gene_hunter.player;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber
public class GeneHunterKeyMappings {

    public static final KeyMapping OPEN_CHOICE_HOLDER = new KeyMapping("key.gene_hunter.open_choice_holder", 67, "key.categories.gene_hunter");

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHOICE_HOLDER);
    }


    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (OPEN_CHOICE_HOLDER.isDown()) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                // 通过 RPC 发送包到服务器请求打开 UI
                RPCPacketDistributor.rpcToServer("gene_hunter:open_choice_menu");
            }
        }
    }

}
