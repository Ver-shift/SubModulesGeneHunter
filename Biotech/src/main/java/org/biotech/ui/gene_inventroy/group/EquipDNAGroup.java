package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.world.entity.player.Player;
import org.biotech.api.BiotechAPI;
import org.biotech.ui.IScalable;
import org.biotech.ui.gene_inventroy.element.DNA;
import org.biotech.ui.gene_inventroy.element.EquipGeneSlot;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;

public class EquipDNAGroup extends UIElement implements IScalable {

    public static final int basePaddingLeft = 40;
    public static final int basePaddingBottom = 15;
    public static final int baseWidth = 85;

    public static final int baseEmptyHeight = 0;
    public static final int baseEmptyContainerWidth = 0;

    private UIElement equipSlotsContainer;
        private List<EquipGeneSlot> equipSlots = new java.util.ArrayList<>();

    private UIElement emptyContainer;

    private UIElement dnaContainer;
        private UIElement empty;
        private DNA dna;

    private UIElement emptyContainer2;

    private float rotationAngle = 0.0f;

    private Player player;
    private IDynamicStackHandler stackHandler;

    public static final int SLOT_COUNT = 6;

    public EquipDNAGroup(Player player) {

        initPlayerData(player);

        this.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
            layout.width(baseWidth);
            layout.heightPercent(100);
            layout.paddingLeft(basePaddingLeft);
            layout.paddingBottom(basePaddingBottom);
        });

        this.equipSlotsContainer = new UIElement();
        equipSlotsContainer.setId("equip_slots_container");
        equipSlotsContainer.layout(layout -> {
            layout.heightPercent(100f);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.justifyContent(AlignContent.SPACE_AROUND);


        });
        this.addChild(equipSlotsContainer);
            // 创建6个槽位
            createSlots();
        this.emptyContainer = new UIElement();
        emptyContainer.setId("empty_container_1");
        emptyContainer.layout(layout -> {
            layout.width(baseEmptyContainerWidth);
            layout.heightPercent(100f);
        });
        this.addChild(emptyContainer);

        this.dnaContainer = new UIElement();
        dnaContainer.layout(layout -> {
            layout.flexDirection(FlexDirection.COLUMN_REVERSE);
        });
        this.addChild(dnaContainer);

            this.empty = new UIElement();
            empty.layout(layout -> {
                layout.height(baseEmptyHeight);
                layout.width(DNA.baseWidth);
            });
            dnaContainer.addChild(empty);

            this.dna = new DNA();
            dna.setId("equip_dna");
            dnaContainer.addChild(dna);

        this.emptyContainer2 = new UIElement();
        emptyContainer2.setId("empty_container_2");
        emptyContainer2.layout(layout -> {
            layout.width(baseEmptyContainerWidth);
            layout.heightPercent(100f);
        });
        this.addChild(emptyContainer2);
    }

    public void initPlayerData(Player player){
        this.player = player;
        this.stackHandler = BiotechAPI.getGeneEquipSlots(player);
    }

    private void createSlots(){
        for (int i = 0; i < SLOT_COUNT; i++) {
            EquipGeneSlot slot = new EquipGeneSlot();
            slot.setId("equip_slot_" + i);
            slot.slotStyle(style -> {
                style.acceptQuickMove(true);
                style.isPlayerSlot(false);
                style.quickMovePriority(200);
            });

            // 绑定到 stackHandler
            if (stackHandler != null) {
                slot.bind(stackHandler, i);
            }

            equipSlots.add(slot);
            equipSlotsContainer.addChild(slot);
        }
    }
    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.paddingLeft(basePaddingLeft * scale);
            layout.paddingBottom(basePaddingBottom * scale);
            layout.width(baseWidth * scale);
        });



        for (EquipGeneSlot slot : equipSlots) {
            slot.scale(scale);
        }
        empty.layout(layout -> {
            layout.width(DNA.baseWidth * scale);
            layout.heightPercent(baseEmptyHeight * scale);
        });
        emptyContainer.layout(layout -> {
            layout.width(baseEmptyContainerWidth * scale);
        });
        emptyContainer2.layout(layout -> {
            layout.width(baseEmptyContainerWidth * scale);
        });

        dna.scale(scale);
    }

    /**
     * 设置旋转角度（使用 Transform2D）
     */
    public void setRotation(float angle) {
        this.rotationAngle = angle;
        this.transform(t -> t.rotation(angle));
        for (EquipGeneSlot slot : equipSlots) {
            slot.transform(t -> t.rotation(-angle));
        }
    }

}
