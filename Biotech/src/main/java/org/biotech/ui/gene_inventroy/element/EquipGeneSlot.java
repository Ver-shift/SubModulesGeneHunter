package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.biotech.Biotech;
import org.biotech.item.GeneItem;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;
import org.biotech.ui.animation.IBaseAnimation;
import org.biotech.ui.animation.ShowyAnimation;

public class EquipGeneSlot extends ItemSlot implements IScalable {

    public static final int basePaddingLeftOrRight = 10;
    public static final int baseWidth = 24;
    public static final int baseHeight = 25;

    // 浮动动画
    private IBaseAnimation floatAnimation;
    private float currentScale = 1.0f;

    public static final IGuiTexture slotOverlay = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(0,72,24,25);

    public static final IGuiTexture hoverOverlay = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(25, 72, 24, 25);

    public EquipGeneSlot() {
        super();
        this.floatAnimation = new ShowyAnimation(0.5f, 0.005f, 0.025f);
        this.layout(layout ->{
            layout.width(baseWidth);
            layout.height(baseHeight);
        });

        this.slotStyle(style->{

            style.slotOverlay(slotOverlay);
            style.hoverOverlay(hoverOverlay);
            style.acceptQuickMove(true);
            style.isPlayerSlot(false);
            style.quickMovePriority(200);
        });
        this.addEventListener(UIEvents.TICK, event -> {
            floatAnimation.tick();
        });
    }

    @Override
    public void drawBackgroundTexture(GUIContext guiContext) {

    }


    @Override
    protected void drawSlotOverlay(GUIContext guiContext) {
        guiContext.pose.pushPose();
        floatAnimation.animation(guiContext, currentScale);
        super.drawSlotOverlay(guiContext);
        guiContext.pose.popPose();
    }

    @Override
    protected void drawHover(GUIContext guiContext) {
        if (!getSlot().hasItem()){
            guiContext.pose.pushPose();
            floatAnimation.animation(guiContext, currentScale);
            super.drawHover(guiContext);
            guiContext.pose.popPose();
        }



    }

    @Override
    protected void drawItemStack(GUIContext guiContext, ItemStack itemStack) {

        if (itemStack.getItem() instanceof GeneItem){
            ResourceLocation textureResource = Biotech.asResource("textures/item/gene_item.png");
            IGuiTexture texture = SpriteTexture.of(textureResource);

            guiContext.pose.pushPose();
            floatAnimation.animation(guiContext, currentScale);
            guiContext.drawTexture(texture, 0, 0, 18, 18);

            guiContext.pose.popPose();

        }
    }

    @Override
    public void scale(float scale) {
        this.currentScale = scale;
        this.layout(layout -> {
            layout.width((baseWidth) * scale);
            layout.height(baseHeight * scale);
//            layout.paddingLeft(basePaddingLeftOrRight * scale);
//            layout.paddingRight(basePaddingLeftOrRight * scale);
        });
    }
}
