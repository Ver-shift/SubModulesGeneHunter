package org.galaxy.gene_hunter.container;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.galaxy.gene_hunter.ui.element.Choice;
import org.jetbrains.annotations.Nullable;

import static org.galaxy.gene_hunter.api.GeneHunterAPI.getChoiceManager;
import static org.galaxy.gene_hunter.api.GeneHunterAPI.getGeneHunterData;

public class ChoiceContainer {


    public static ModularUI init(Player player) {
        return createGeneInventoryUI(player);
    }
    public static ModularUI createGeneInventoryUI(Player player) {

        var root = new UIElement();
        root.layout(layout -> {
            layout.heightPercent(100f);
            layout.widthPercent(100f);
            layout.flexDirection(FlexDirection.ROW);
            layout.justifyContent(AlignContent.SPACE_AROUND);
            layout.alignItems(AlignItems.CENTER);    // Y轴（交叉轴）居中
        });

        // Important: menu slot structure must be identical on both sides.
        var holderData = getGeneHunterData(player).getChoiceHolderData();
        var handler = holderData.getChoiceHolderHandler();
        int slotCount = Math.min(holderData.getChoiceCount(), handler.getSlots());
        for (int i = 0; i < slotCount; i++) {
            var choice = new Choice();
            root.addChild(choice);
            choice.getSlot().bind(handler, i);
        }

        var button = new Button(){
            @Override
            public Button setOnClick(@Nullable UIEventListener onClick) {
                RPCPacketDistributor.rpcToServer("gene_hunter:refresh_choice");
                return super.setOnClick(onClick);
            }
        };
        button.layout(layout -> {
            layout.heightPercent(5);
            layout.widthPercent(5);
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(2);
            layout.top(2);
        });

        root.addChild(button);

        return ModularUI.of(UI.of(root), player);
    }
}