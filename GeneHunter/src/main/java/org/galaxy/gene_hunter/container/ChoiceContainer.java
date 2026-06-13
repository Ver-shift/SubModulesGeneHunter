package org.galaxy.gene_hunter.container;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.biotech.api.init.BiotechLootTypeInit;
import org.biotech.ui.BiotechTexture;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxy.gene_hunter.api.system.choice.ChoiceStage;
import org.galaxy.gene_hunter.ui.element.Choice;

import java.util.List;

import static org.galaxy.gene_hunter.api.GeneHunterAPI.getChoiceManager;
import static org.galaxy.gene_hunter.api.GeneHunterAPI.getGeneHunterData;

public class ChoiceContainer {


    public static ModularUI init(Player player) {
        return createGeneInventoryUI(player);
    }

    public static void rogueRewardEvent(ServerPlayer player, NodeColor nodeColor) {
        getChoiceManager(player).startRoll(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get(), nodeColor);
    }

    public static void bossRewardEvent(ServerPlayer player, NodeColor nodeColor) {
        getChoiceManager(player).startStages(List.of(
                ChoiceStage.fixed(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get(), nodeColor, 3),
                ChoiceStage.of(GeneHunterLootInit.WEAPON_LOOT_TYPE.get(), nodeColor)
        ));
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
            choice.getSlot().setSlotIndex(i);
        }

        var button = new Button();
        button.layout(layout -> {
            layout.height(22);
            layout.width(92);
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(2);
            layout.top(2);
        });
        button.buttonStyle(style -> {
            style.baseTexture(BiotechTexture.Button_Slice);
            style.hoverTexture(BiotechTexture.Button_Slice);
            style.pressedTexture(BiotechTexture.Button_Slice);
        });
        button.textStyle(style -> {
            style.fontSize(8);
            style.textColor(0xFFFFFF);
            style.textShadow(true);
        });
        button.addEventListener(UIEvents.TICK, event -> {
            int cost = holderData.getRefreshCost();
            button.setText(Component.translatable("gene_hunter.choice.refresh.cost", cost));
        });
        button.addEventListener(UIEvents.CLICK, event -> {
            RPCPacketDistributor.rpcToServer("gene_hunter:refresh_choice");
        });

        root.addChild(button);

        return ModularUI.of(UI.of(root), player);
    }
}
