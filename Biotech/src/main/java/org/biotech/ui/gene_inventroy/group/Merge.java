package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.biotech.api.BiotechAPI;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.gene_inventroy.element.BaseRoot;
import org.biotech.ui.gene_inventroy.element.merge.MergeInputSlotLine;

public class Merge extends BaseRoot {

    private UIElement geneInventorySlotsRoot;
        private UIElement empty;
        private GeneInventoryGroup geneInventoryGroup;

    private UIElement workRoot;
        private UIElement squareContainer;
            private UIElement square;
        private UIElement outputRoot;
            private OutputGroup outputGroup;
        private UIElement inoutRoot;
            private MergeInputSlotLine inputLine;

    private Player player;
    private IItemHandlerModifiable mergeInputHandler;


    public Merge(Player player) {
        super();
        this.layout(layout->{
            layout.flexDirection(FlexDirection.COLUMN_REVERSE);
        });
        this.setId("merge");
        initData(player);
        initLayout();

    }

    private void initData(Player player){
        this.player = player;
        this.mergeInputHandler = BiotechAPI.getGeneData(player).getMergeData().getInputSlots();
        var mergeManager = BiotechAPI.getMergeManager(player);
        if (mergeManager != null) {
            mergeManager.updateSlotData();
        }

    }
    private void initLayout() {
        this.geneInventorySlotsRoot = new UIElement();
        geneInventorySlotsRoot.setId("gene_inventory_slots_root");
        geneInventorySlotsRoot.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(50);
            layout.alignItems(AlignItems.CENTER);

        });
        this.addChild(geneInventorySlotsRoot);
            this.empty = new UIElement();
            empty.layout(layout -> {
                layout.widthPercent(10f);
                layout.heightPercent(100f);
            });
//            geneInventorySlotsRoot.addChild(empty);
            float padding = 17f;
            this.geneInventoryGroup = new GeneInventoryGroup(player,0,2);
            geneInventoryGroup.layout(layout -> {
                layout.paddingLeft(padding);
            });
            geneInventorySlotsRoot.addChild(geneInventoryGroup);


        this.workRoot = new UIElement();
        workRoot.setId("work_root");
        workRoot.layout(layout -> {
            layout.widthPercent(100f);
            layout.heightPercent(50f);
            layout.flexShrink(1);
            layout.flexDirection(FlexDirection.COLUMN_REVERSE);
        });
        this.addChild(workRoot);

            this.squareContainer = new UIElement();
            squareContainer.layout(layout -> {
                layout.widthPercent(100f);
                layout.heightPercent(7f);
                layout.justifyContent(AlignContent.CENTER); // 主轴居中
                layout.alignItems(AlignItems.CENTER);       // 交叉轴居中
            });

            workRoot.addChild(squareContainer);

                this.square = new UIElement();
                square.layout(layout -> {
                    layout.widthPercent(70f);
                    layout.heightPercent(100f);
                });
                square.style(style -> {
                    style.background(BiotechTexture.Button_Slice);
                });
                squareContainer.addChild(square);

            this.outputRoot = new UIElement();
            outputRoot.setId("output_root");
            outputRoot.layout(layout -> {
                layout.widthPercent(100f);
            });
            workRoot.addChild(outputRoot);

                this.outputGroup = new OutputGroup(player);
                outputRoot.addChild(outputGroup);


            this.inoutRoot = new UIElement();
            inoutRoot.setId("inout_root");
            inoutRoot.layout(layout -> {
                layout.widthPercent(100f);
                layout.alignItems(AlignItems.CENTER);
            });
            workRoot.addChild(inoutRoot);

                // 添加合并输入槽位行（一行，9个槽位）
                int inputSlotCount = mergeInputHandler != null ? mergeInputHandler.getSlots() : 0;
                this.inputLine = new MergeInputSlotLine(0, 0f, 0, inputSlotCount, mergeInputHandler);
                inputLine.setId("merge_input_line");
                inoutRoot.addChild(inputLine);
    }

    @Override
    protected void scaleTick() {
        if (!isScaleDirty()) return;

        float scale = getCurrentScale();
        float padding = 17f;

        // 应用缩放到所有子组件
        geneInventoryGroup.scale(scale);
        geneInventoryGroup.layout(layout -> {
            layout.paddingLeft(padding * scale);
        });

        outputGroup.scale(scale);

        // 对 inputLine 应用缩放
        inoutRoot.getChildren().forEach(child -> {
            if (child instanceof MergeInputSlotLine inputLine) {
                inputLine.scale(scale);
            }
        });
    }
}
