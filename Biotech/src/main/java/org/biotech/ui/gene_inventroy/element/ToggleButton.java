package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import org.biotech.ui.IScalable;

public class ToggleButton extends Button implements IScalable {

    public static final int baseWidth = 25;

    public ToggleButton() {
        super();
        this.layout(layout->{
            layout.width(baseWidth);
            layout.height(baseWidth);
        });
    }


    @Override
    public void scale(float scale) {
        this.layout(layout->{
            layout.width(baseWidth*scale);
            layout.height(baseWidth*scale);
        });
    }
}
