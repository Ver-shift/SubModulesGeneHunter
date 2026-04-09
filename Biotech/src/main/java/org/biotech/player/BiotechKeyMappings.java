package org.biotech.player;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.biotech.api.BiotechAPI;

@EventBusSubscriber
public class BiotechKeyMappings {

    public static final KeyMapping OPEN_CHOICE_HOLDER = new KeyMapping("key.biotech.open_gene_menu",67 , "key.categories.biotech");

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
                RPCPacketDistributor.rpcToServer("biotech:open_gene_menu");
            }
        }
    }
    /**
     * RPC 方法：服务器端处理客户端请求打开选择菜单
     */
    @RPCPacket("biotech:open_gene_menu")
    public static void openChoiceMenu(RPCSender sender) {
        if (sender.isRemote()) {
            var player = sender.asPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                BiotechAPI.openGeneInventoryFor(serverPlayer);
            }
        }
    }
}
