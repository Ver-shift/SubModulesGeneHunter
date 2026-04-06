package org.galaxy.gene_hunter.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;

public class Choice extends UIElement {

    private UIElement item_root;
        private ItemSlot slot;
    private UIElement description_root;
        private Label label;


    public Choice(){
        super();

        this.item_root = new UIElement();
        this.addChild(item_root);
            this.slot = new ItemSlot();
            this.addChild(slot);

        this.description_root = new UIElement();
        this.addChild(description_root);
            this.label = new Label();
            this.addChild(label);
    }
}
