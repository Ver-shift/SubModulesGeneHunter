package org.galaxy.gene_hunter.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.gene_inventroy.element.BaseRoot;

public class Choice extends UIElement {

    private static final int TEXT_COLOR = 0xFFFFFF;

    private UIElement item_root;
        private BaseRoot base_root;
            @Setter @Getter
            private DisplaySlot slot;
    private UIElement description_root;
        private Label label;

    private IItemHandlerModifiable itemHandler;
    private int itemIndex = -1;


    public Choice(){
        super();
        this.layout(layoutStyle -> {
            layoutStyle.heightPercent(50);
            layoutStyle.widthPercent(20);
        });


        //item---------------------
        this.item_root = new UIElement();
        item_root.layout(layoutStyle -> {
            layoutStyle.heightPercent(80);
            layoutStyle.widthPercent(100);
            layoutStyle.justifyContent(AlignContent.SPACE_AROUND);
            layoutStyle.alignItems(AlignItems.CENTER);    // Y轴（交叉轴）居中
        });
        item_root.style(style -> {
            style.backgroundTexture(BiotechTexture.Button_Slice);
        });
        this.addChild(item_root);

            this.base_root = new BaseRoot();
            base_root.setBaseHeight(18);
            base_root.setBaseWidth(18);
            base_root.layout(layoutStyle -> {
                layoutStyle.justifyContent(AlignContent.SPACE_AROUND);
                layoutStyle.alignItems(AlignItems.CENTER);    // Y轴（交叉轴）居中
            });
            item_root.addChild(base_root);

                this.slot = new DisplaySlot();
                this.addEventListener(UIEvents.TICK, event -> {
                    slot.scale(base_root.getCurrentScale());
                });
                base_root.addChild(slot);

        //description---------------------------
        this.description_root = new UIElement();
        description_root.layout(layoutStyle -> {
            layoutStyle.heightPercent(20);
            layoutStyle.widthPercent(100);
        });
        description_root.style(style -> {
            style.backgroundTexture(BiotechTexture.Button_Slice);
        });
        this.addChild(description_root);
            this.label = new Label();
            label.setText(Component.empty());
            label.textStyle(style -> {
                style.textWrap(TextWrap.HIDE);
                style.textColor(TEXT_COLOR);
                style.textShadow(true);
                style.textAlignHorizontal(Horizontal.CENTER);
                style.textAlignVertical(Vertical.CENTER);
            });
            label.layout(layoutStyle -> {
                layoutStyle.heightPercent(100);
                layoutStyle.widthPercent(100);
            });
            description_root.addChild(label);
    }

    public void bind(IItemHandlerModifiable itemHandler, int itemIndex) {
        this.itemHandler = itemHandler;
        this.itemIndex = itemIndex;
        this.slot.bind(itemHandler, itemIndex);
        this.slot.setSlotIndex(itemIndex);
        updateLabel();
        this.addEventListener(UIEvents.TICK, event -> updateLabel());
    }

    private void updateLabel() {
        if (itemHandler == null || itemIndex < 0 || itemIndex >= itemHandler.getSlots()) {
            label.setText(Component.empty());
            return;
        }

        ItemStack stack = itemHandler.getStackInSlot(itemIndex);
        if (stack.isEmpty()) {
            label.setText(Component.empty());
            return;
        }

        // Genes and Xenes are now datapack definitions, so the stack name is
        // the canonical label and no retired Trait API lookup is required.
        label.setText(stack.getHoverName());
    }
}
