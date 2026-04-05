package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.world.entity.player.Player;
import org.biotech.ui.gene_inventroy.element.BaseRoot;

/**
 * 库存切换布局容器
 * 包含 inventory_root (库存) 和 info_root (信息) 两个子区域
 */
public class Inventory extends BaseRoot {

    private UIElement inventoryInfoRoot;
        private UIElement inventoryRoot;
            private GeneInventoryGroup geneInventoryGroup;
        private UIElement infoRoot;
            private InfoGroup infoGroup;

    private UIElement equipDNARoot;
        private EquipDNAGroup equipDNAGroup;

    private Player player;

    public Inventory(Player player) {
        super();
        this.player = player;
        this.setId("inventory");
        this.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW_REVERSE);
        });
        initLayout();
    }

    private void initLayout() {


        //存储部分;
        this.inventoryInfoRoot = new UIElement();
        inventoryInfoRoot.setId("inventory_info_root");
        inventoryInfoRoot.layout(layout -> {
            layout.widthPercent(61.8f);
            layout.heightPercent(100);
            layout.flexDirection(FlexDirection.COLUMN_REVERSE);
        });
        this.addChild(inventoryInfoRoot);

            this.inventoryRoot = new UIElement();
            inventoryRoot.setId("inventory_root");
            inventoryRoot.layout(layout -> {
                layout.widthPercent(100);
                layout.heightPercent(50);
            });
            inventoryInfoRoot.addChild(inventoryRoot);

                this.geneInventoryGroup = new GeneInventoryGroup(player);
                geneInventoryGroup.setId("gene_inventory_group");
                inventoryRoot.addChild(geneInventoryGroup);


            this.infoRoot = new UIElement();
            infoRoot.setId("info_root");
            infoRoot.layout(layout -> {
                layout.widthPercent(100);
                layout.heightPercent(50);
                layout.flexDirection(FlexDirection.COLUMN_REVERSE);
            });
            inventoryInfoRoot.addChild(infoRoot);

                this.infoGroup = new InfoGroup(player);
                infoGroup.setId("info_group");
                infoRoot.addChild(infoGroup);

        //装备和dna部分
        this.equipDNARoot = new UIElement();
        equipDNARoot.setId("equip_dna_root");
        equipDNARoot.layout(layout -> {
            layout.widthPercent(50);
            layout.heightPercent(100);
            layout.flexShrink(1);
            layout.flexDirection(FlexDirection.ROW_REVERSE);
        });
        this.addChild(equipDNARoot);

            this.equipDNAGroup = new EquipDNAGroup(player);
            equipDNAGroup.setId("equip_dna_group");
            equipDNAGroup.setRotation(28.44f);
            equipDNARoot.addChild(equipDNAGroup);
    }

    @Override
    protected void scaleTick() {
        if (!isScaleDirty()) return;

        float scale = getCurrentScale();

        // 应用缩放到所有子组件
        geneInventoryGroup.scale(scale);
        infoGroup.scale(scale);
        equipDNAGroup.scale(scale);    }



}
