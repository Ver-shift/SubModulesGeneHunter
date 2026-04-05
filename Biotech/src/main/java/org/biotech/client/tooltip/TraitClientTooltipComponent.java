package org.biotech.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.biotech.api.tooltip.TraitTooltipComponent;
import org.joml.Matrix4f;

/**
 * Client renderer for TraitTooltipComponent.
 */
public class TraitClientTooltipComponent implements ClientTooltipComponent {

    private static final int ICON_SIZE = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int GAP = 4;

    private final TraitTooltipComponent data;

    public TraitClientTooltipComponent(TraitTooltipComponent data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        int lines = 0;
        for (TraitTooltipComponent.Entry entry : this.data.entries()) {
            lines += 1; // title line
            lines += Math.max(1, entry.descriptions().size()); // at least one description line
        }
        return Math.max(1, lines) * LINE_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;
        for (TraitTooltipComponent.Entry entry : this.data.entries()) {
            width = Math.max(width, ICON_SIZE + GAP + font.width(entry.name()));
            if (entry.descriptions().isEmpty()) {
                continue;
            }
            for (Component desc : entry.descriptions()) {
                width = Math.max(width, ICON_SIZE + GAP + font.width(desc));
            }
        }
        return width;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        // Text is rendered together with icons in renderImage so layout stays aligned.
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int currentY = y;
        for (TraitTooltipComponent.Entry entry : this.data.entries()) {
            if (entry.texture() != null) {
                guiGraphics.blit(entry.texture(), x, currentY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            guiGraphics.drawString(font, entry.name(), x + ICON_SIZE + GAP, currentY, 0xFFE0E0E0, false);
            currentY += LINE_HEIGHT;

            if (entry.descriptions().isEmpty()) {
                continue;
            }

            for (Component desc : entry.descriptions()) {
                guiGraphics.drawString(font, desc, x + ICON_SIZE + GAP, currentY, 0xFF9FD49F, false);
                currentY += LINE_HEIGHT;
            }
        }
    }
}

