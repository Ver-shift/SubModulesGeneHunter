package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.world.entity.player.Player;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;
import org.biotech.ui.gene_inventroy.element.merge.MergeOutputSlot;


/**
 * gene slot gene
 */
public class OutputGroup extends UIElement implements IScalable {

    public static final int baseWidth = 114;
    public static final int baseHeight = 19;
    public static final IGuiTexture dna = SpriteTexture
            .of(BiotechTexture.DNA_TWO)
            .setSprite(0,0, baseWidth, baseHeight);

    private UIElement dna_left;
    private MergeOutputSlot outputSlot;
    private UIElement dna_right;

    public OutputGroup(Player player){
        this.layout(layoutStyle -> {
            layoutStyle.heightPercent(100f);
            layoutStyle.flexDirection(FlexDirection.ROW);
        });

        this.dna_left = new UIElement();
        dna_left.setId("dna_left");
        dna_left.style(style -> {
            style.backgroundTexture(OutputGroup.dna);
        });
        this.addChild(dna_left);

        this.outputSlot = new MergeOutputSlot(player);
        outputSlot.setId("output_slot");
        this.addChild(outputSlot);

        this.dna_right = new UIElement();
        dna_right.setId("dna_right");
        dna_right.style(style -> {
            style.backgroundTexture(OutputGroup.dna);
        });
        this.addChild(dna_right);
    }

    @Override
    public void scale(float scale) {

        float offY = 12f;


        dna_left.layout(layoutStyle -> {
            layoutStyle.width(baseWidth * scale);
            layoutStyle.height(baseHeight * scale);
        });
        dna_left.style(style -> {
            style.transform2D(new Transform2D().translate(0,offY *scale));
        });

        outputSlot.scale(scale);

        dna_right.layout(layoutStyle -> {
            layoutStyle.width(baseWidth * scale);
            layoutStyle.height(baseHeight * scale);
        });
        dna_right.style(style -> {
            style.transform2D(new Transform2D().translate(0,offY *scale));
        });


    }
}
