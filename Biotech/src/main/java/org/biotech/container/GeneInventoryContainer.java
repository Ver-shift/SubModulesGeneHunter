package org.biotech.container;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.world.entity.player.Player;
import org.biotech.api.BiotechAPI;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.gene_inventroy.element.GeneBackGround;
import org.biotech.ui.gene_inventroy.group.ElementGroup;
import org.biotech.ui.gene_inventroy.group.Inventory;
import org.biotech.ui.gene_inventroy.group.Merge;

/**
 * 基因库存容器
 * 使用 GeneInventoryGroup 管理 3 行 × 27 槽位 = 81 个槽位
 */
public class GeneInventoryContainer {

    public static ModularUI init(Player player) {
        return createGeneInventoryUI(player);
    }

    public static ModularUI createGeneInventoryUI(Player player) {

        //==============1.初始化==============
        var inventoryData = BiotechAPI.getGeneData(player).getPlayerGeneInventoryData();
        var geneSlots = inventoryData.getGeneSlots();
        var favoritesSlots = inventoryData.getFavoriteSlots();

        //===============2.基础容器构建=========
        // Root - 全屏容器，Flexbox 居中
        var root = new UIElement();
        root.setId("root");
        root.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.justifyContent(AlignContent.CENTER);  // 主轴居中
            layout.alignItems(AlignItems.CENTER);          // 交叉轴居中
        });
        // 背景
        var background = new GeneBackGround();
        background.setId("background");
        root.addChild(background);

        //切换布局组
        ElementGroup toggleGeneGroup = new ElementGroup();
        toggleGeneGroup.setId("element_gene_group");
        toggleGeneGroup.addEventListener(UIEvents.TICK, event -> {
            float scale = toggleGeneGroup.getCurrentScale();
            background.scale(scale);
        });
        root.addChild(toggleGeneGroup);

        //==============3 布局==================
        String inventory = "inventory";
        toggleGeneGroup
                .addChild(inventory, new Inventory(player))
                .addChildTexture(inventory, SpriteTexture
                        .of(BiotechTexture.GUI_TEXTURE)
                        .setSprite(0,46,25,25))
                .addHoveredTexture(inventory, SpriteTexture
                        .of(BiotechTexture.GUI_TEXTURE)
                        .setSprite(26,46,25,25));

        String merges = "merges";
        toggleGeneGroup
                .addChild(merges,new Merge(player))
                .addChildTexture(merges, SpriteTexture
                .of(BiotechTexture.GUI_TEXTURE)
                .setSprite(0,20,25,25))
                .addHoveredTexture(merges, SpriteTexture
                        .of(BiotechTexture.GUI_TEXTURE)
                        .setSprite(26,20,25,25));

        return ModularUI.of(UI.of(root), player);
    }



    public enum UIState{
        /**
         * 初始化阶段，UI 正在场外进行
         */
        INIT,
        LOADING,
        NORMAL,
        END,

    }
}
