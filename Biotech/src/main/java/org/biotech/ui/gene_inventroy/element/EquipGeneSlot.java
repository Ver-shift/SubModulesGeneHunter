package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.system.trait.core.IStackTraitAccess;
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
    private static final float ROMAN_TEXT_SCALE = 0.75f;

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
            ResourceLocation textureResource = IStackTraitAccess.getSelectedTraitTexture(itemStack);
            if (textureResource == null) {
                return;
            }
            IGuiTexture texture = SpriteTexture.of(textureResource);

            guiContext.pose.pushPose();
            floatAnimation.animation(guiContext, currentScale);
            guiContext.drawTexture(texture, 0, 0, 18, 18);

            int traitCount = IStackTraitAccess.getTraitCount(itemStack);
            if (traitCount > 1) {
                Font font = Minecraft.getInstance().font;
                String roman = toRoman(traitCount);
                int badgeX = 17 - Math.round(font.width(roman) * ROMAN_TEXT_SCALE);
                int badgeY = 11;

                guiContext.pose.pushPose();
                // Raise Z so the badge is always above slot/icon overlays.
                guiContext.pose.translate(badgeX, badgeY, 200);
                guiContext.pose.scale(ROMAN_TEXT_SCALE, ROMAN_TEXT_SCALE, 1.0F);
                guiContext.graphics.drawString(font, roman, 0, 0, 0xFFFFE6FF, true);
                guiContext.pose.popPose();
            }

            guiContext.pose.popPose();

        }
    }


    private static String toRoman(int value) {
        int[] numbers = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder result = new StringBuilder();
        int remaining = Math.max(value, 0);

        for (int i = 0; i < numbers.length && remaining > 0; i++) {
            while (remaining >= numbers[i]) {
                result.append(symbols[i]);
                remaining -= numbers[i];
            }
        }
        return result.toString();
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
