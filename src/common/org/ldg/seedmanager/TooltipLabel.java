package org.ldg.seedmanager;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;

public class TooltipLabel extends TooltipButton {
    public TooltipLabel(int id, int x, int y, int w, int h, String label) {
        super(id, x, y, w, h, label);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouse_x, int mouse_y) {
        this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition, 0x404040);
    }

    public void drawCenteredString(FontRenderer fontRenderer, String s,
                                   int x, int y, int color) {
        fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s) / 2, y,
                                color);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouse_x, int mouse_y) {
        return false;
    }
}
