import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class SeedLibraryGUI extends GuiContainer
{
    public SeedLibraryTileEntity seedlibrary;
    public static final int BORDER = 4;
    public int main_width, main_height, left, top, center, middle, right, bottom, sliders_x, sliders_y, sliders_spacing;
    public int current_slider = -1, drag_start_x = 0, drag_start_value = 0;
    public SeedLibraryGUI(InventoryPlayer inventoryplayer, TileEntity seedmanager)
    {
        super(new SeedLibraryContainer(inventoryplayer, (SeedLibraryTileEntity)seedmanager));
        seedlibrary = (SeedLibraryTileEntity) seedmanager;


        ySize = 222;

        main_width = xSize - BORDER * 2;
        main_height = (ySize - 96) - BORDER * 2 - 18*2;

        left = BORDER;
        top = BORDER;
        center = left + main_width/2;
        middle = top + main_height/2;
        right = left + main_width;
        bottom = top + main_height;

        sliders_x = center + main_width / 4 - (63/2);
        sliders_y = top + 2 + 9 - 1;
        sliders_spacing = 11 + 9;

    }

    public void drawCenteredString(String s, int x, int y, int color) {
        fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s) / 2, y,
                                color);
    }

    public void draw3DRect(int left, int top, int right, int bottom) {
        drawRect(left, top, right, bottom, 0xff373737);
        drawRect(left+1, top+1, right, bottom, 0xffffffff);
        drawRect(left+1, top+1, right-1, bottom-1, 0xffc6c6c6);
    }

    protected void drawGuiContainerForegroundLayer()
    {
        drawCenteredString("Seed Type", left + main_width / 4, top + 2,
                           0x404040);
        drawCenteredString(seedlibrary.getGUIFilter().getCropName(),
                           left + main_width / 4, top + 2 + 8 + 1 + 18 + 2,
                           0x404040);

        drawCenteredString("Growth", center + main_width / 4, top + 2,
                           0x404040);
        drawCenteredString("Gain", center + main_width / 4, top + 2 + 9 + 11,
                           0x404040);
        drawCenteredString("Resistance", center + main_width / 4, 
                           top + 2 + (9 + 11)*2, 0x404040);
        drawCenteredString("Total", center + main_width / 4, 
                           top + 2 + (9 + 11)*3, 0x404040);

        String count = seedlibrary.getGUISeedCount() + " Seeds";
        int count_width = fontRenderer.getStringWidth(count);
        fontRenderer.drawString(count, 128 - count_width, 88, 0x404040);

        if (seedlibrary.energy <= 0) {
            drawRect(left, top, right, bottom + 20, 0xff000000);
            drawCenteredString("Out of power.", center, middle - 3, 0x404040);
            drawCenteredString("Connect to LV power", center, middle + 6, 0x404040);
            drawCenteredString("or insert a battery.", center, middle + 15, 0x404040);

            // Re-bind the GUI's texture, because something else took over.
            int k = mc.renderEngine.getTexture("/fm_seedlibrary_gui.png");
            mc.renderEngine.bindTexture(k);

            drawTexturedModalRect(left + 3, bottom, 176, 18, 18, 18);
            fontRenderer.drawString("Battery slot", left + 23, bottom + 5,
                                    0x404040);
        }

        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        // Bind the GUI's texture.
        int k = mc.renderEngine.getTexture("/fm_seedlibrary_gui.png");
        mc.renderEngine.bindTexture(k);

        // Ensure the color is standard.
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Transfer the coordinate space to within the GUI screen.
        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft, guiTop, 0.0F);

        // Draw the background.
        drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);

        // Draw the borders for the three upper sections.
        draw3DRect(left, top, center, middle);
        draw3DRect(left, middle, center, bottom);
        draw3DRect(center, top, right, bottom);

        // Draw the dashed outline for getting seed types.
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(left + (main_width/4) - 9, top + 11,
                              176, 54, 18, 18);

        // Draw the faded seed bag in the dashed outline.
        drawTexturedModalRect(left + (main_width/4) - 9, top + 11,
                              194, 0, 18, 18);

        // Draw the sliders and arrows.
        SeedLibraryFilter filter = seedlibrary.getGUIFilter();
        drawSlider(0, filter.min_growth, filter.max_growth);
        drawSlider(1, filter.min_gain, filter.max_gain);
        drawSlider(2, filter.min_resistance, filter.max_resistance);
        drawSlider(3, filter.min_total / 3, filter.max_total / 3);

        // Restore previous coordinates.
        GL11.glPopMatrix();
    }

    public void drawSlider(int index, int min, int max) {
        int pre_size = min * 2;
        int in_size = (max - min) * 2 + 1;
        int post_size = (31 - max) * 2;

        int x = sliders_x;
        int y = sliders_y + 1 + sliders_spacing*index;

        // Black before.
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
        drawTexturedModalRect(x, y, 0, 222, pre_size, 7);

        // Green during.
        GL11.glColor4f(0.0F, 0.5F, 0.0F, 1.0F);
        drawTexturedModalRect(x + pre_size, y, pre_size, 222, in_size, 7);

        // Black after.
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
        drawTexturedModalRect(x + pre_size + in_size, y,
                              pre_size + in_size, 222, post_size, 7);

        // Green arrows.
        GL11.glColor4f(0.0F, 0.5F, 0.0F, 1.0F);
        drawTexturedModalRect(x + pre_size - 2, y-1, 176, 36, 3, 9);
        drawTexturedModalRect(x + pre_size + in_size - 1, y-1, 179, 36, 3, 9);
        // With slight smoothing.
        GL11.glEnable(3042 /*GL_BLEND*/);
        GL11.glColor4f(0.0F, 0.5F, 0.0F, 0.25F);
        drawTexturedModalRect(x + pre_size - 2, y-1, 182, 36, 3, 9);
        drawTexturedModalRect(x + pre_size + in_size - 1, y-1, 185, 36, 3, 9);
        GL11.glDisable(3042 /*GL_BLEND*/);



        // Return to standard colors
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if (seedlibrary.energy <= 0) {
            current_slider = -1;
            return;
        }
        int screen_x = x;
        int screen_y = y;
        if (button == 0) {
            // LMB down.

            // Adjust for GUI coordinates.
            x -= guiLeft;
            y -= guiTop;

            // Set current slider to what's under the mouse, so it can track.
            if (x < (sliders_x - 2) || y < sliders_y) {
                current_slider = -1;
                return; // Above or left of the bars.
            }

            x -= sliders_x;
            y -= sliders_y;

            int bar = y / sliders_spacing;
            int remainder = y % sliders_spacing;
            if (bar > 3 || remainder >= 10) {
                current_slider = -1;
                return; // Below the bars.
            }

            int min = getSliderValue(bar*2);
            int max = getSliderValue(bar*2 + 1);

            if (x < min * 2 - 2) {
                // Left of both arrows.
                current_slider = -1;
            } else if (x <= min * 2) {
                // Over the minimum arrow.
                current_slider = bar * 2;
            } else if (x < max * 2) {
                // Between the arrows.
                current_slider = -1;
            } else if (x <= max * 2 + 2) {
                // Over the maximum arrow;
                current_slider = bar * 2 + 1;
            } else {
                // Right of both arrows.
                current_slider = -1;
            }

            if (current_slider != -1) {
                drag_start_x = screen_x;
                drag_start_value = getSliderValue(current_slider);
            }
        }
    }



    public int getSliderValue(int slider) {
        SeedLibraryFilter filter = seedlibrary.getGUIFilter();
        int bar = slider / 2;
        int arrow = slider % 2;
        if (bar == 0) {
            if (arrow == 0) {
                return filter.min_growth;
            } else {
                return filter.max_growth;
            }
        } else if (bar == 1) {
            if (arrow == 0) {
                return filter.min_gain;
            } else {
                return filter.max_gain;
            }
        } else if (bar == 2) {
            if (arrow == 0) {
                return filter.min_resistance;
            } else {
                return filter.max_resistance;
            }
        } else { // if (bar == 3) 
            if (arrow == 0) {
                return filter.min_total / 3;
            } else {
                return filter.max_total / 3;
            }
        }
    }


    public void setSliderValue(int slider, int value) {
        SeedLibraryFilter filter = seedlibrary.getGUIFilter();
        int bar = slider / 2;
        int arrow = slider % 2;
        if (bar == 0) {
            if (arrow == 0) {
                filter.min_growth = value;
            } else {
                filter.max_growth = value;
            }
        } else if (bar == 1) {
            if (arrow == 0) {
                filter.min_gain = value;
            } else {
                filter.max_gain = value;
            }
        } else if (bar == 2) {
            if (arrow == 0) {
                filter.min_resistance = value;
            } else {
                filter.max_resistance = value;
            }
        } else { // if (bar == 3)
            if (arrow == 0) {
                filter.min_total = value * 3;
            } else {
                filter.max_total = value * 3;
            }
        }

        filter.settingsChanged();
    }


    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        if (seedlibrary.energy <= 0) {
            current_slider = -1;
            return;
        }
        if (button == -1) {
            // Mouse moved.
            // If we're following the mouse with a slider, move it.
            if (current_slider != -1) {
                int value = drag_start_value + (x - drag_start_x) / 2;
                if (value < 0) {
                    value = 0;
                } else if (value > 31) {
                    value = 31;
                }

                int bar = (current_slider / 2);
                int min = getSliderValue(bar * 2);
                int max = getSliderValue(bar * 2 + 1);
                boolean is_max = (current_slider % 2) == 1;
                if (is_max && min > value) {
                    value = min;
                } else if (!is_max && max < value) {
                    value = max;
                }

                if (getSliderValue(current_slider) != value) {
                    setSliderValue(current_slider, value);
                }
            }
        } else if (button == 0) {
            // LMB up.
            // Stop tracking the mouse with a slider.
            if (current_slider != -1) {
                current_slider = -1;
            }
        }
    }
}
