import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import ic2.common.GuiIconButton;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;

public class SeedLibraryGUI extends GuiContainer
{
    public final String BLACK = "\u00A70";
    public final String DARK_BLUE = "\u00A71";
    public final String DARK_GREEN = "\u00A72";
    public final String DARK_AQUA = "\u00A73";
    public final String DARK_RED = "\u00A74";
    public final String DARK_PURPLE = "\u00A75";
    public final String GOLD = "\u00A76";
    public final String GRAY = "\u00A77";
    public final String DARK_GRAY = "\u00A78";
    public final String BLUE = "\u00A79";
    public final String GREEN = "\u00A7A";
    public final String AQUA = "\u00A7B";
    public final String RED = "\u00A7C";
    public final String LIGHT_PURPLE = "\u00A7D";
    public final String YELLOW = "\u00A7E";
    public final String WHITE = "\u00A7F";

    public int mouseX = -1;
    public int mouseY = -1;

    protected List realControls = null;
    protected List noControls = new ArrayList();
    private static java.lang.reflect.Field textureY = null;
    private boolean rightClick = false;
    private GuiButton rightSelect;
    static {
        try {
            textureY = GuiIconButton.class.getDeclaredField("textureY");
            textureY.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public int world_x, world_y, world_z;
    public static final int BORDER = 4;
    public int main_width, main_height, left, top, center, middle, right, bottom, sliders_x, sliders_y, sliders_spacing;
    public int current_slider = -1, drag_start_x = 0, drag_start_value = 0;
    public GuiIconButton unk_type_button, unk_ggr_button;

    public SeedLibraryGUI(InventoryPlayer inventoryplayer, TileEntity seedmanager)
    {
        super(new SeedLibraryContainer(inventoryplayer, (SeedLibraryTileEntity)seedmanager));

        world_x = seedmanager.xCoord;
        world_y = seedmanager.yCoord;
        world_z = seedmanager.zCoord;


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

    public SeedLibraryTileEntity getLibrary() {
        World world = Minecraft.getMinecraft().thePlayer.worldObj;
        TileEntity te = world.getBlockTileEntity(world_x, world_y, world_z);

        if (te instanceof SeedLibraryTileEntity) {
            return (SeedLibraryTileEntity) te;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        GuiIconButton importButton = new GuiIconButton(0, guiLeft + 132, guiTop + 86, 18, 20, "/fm_seedlibrary_gui.png", 176+2, 0+1);
        controlList.add(importButton);
        GuiIconButton exportButton = new GuiIconButton(1, guiLeft + 151, guiTop + 86, 18, 20, "/fm_seedlibrary_gui.png", 176+2, 18+1);
        controlList.add(exportButton);

        unk_type_button = new GuiIconButton(2, guiLeft + left + main_width/8 - 9, guiTop + middle + 20, 18, 20, "/fm_seedlibrary_gui.png", 176+2, 72+1);
        controlList.add(unk_type_button);
        unk_ggr_button = new GuiIconButton(3, guiLeft + left + (main_width*3)/8 - 9, guiTop + middle + 20, 18, 20, "/fm_seedlibrary_gui.png", 176+2, 72+1);
        controlList.add(unk_ggr_button);

        int x = guiLeft + left + 3;
        int y = guiTop + 86;
        for (int dir=0; dir<6; dir++) {
            // Down = -Y = 0
            // Up = +Y = 1
            // North = -Z = 2
            // South = +Z = 3
            // West = -X = 4
            // East = +X = 5
            String key = "BTNSEW";
            String name = "" + key.charAt(dir);
            controlList.add(new GuiButton(dir + 4, x + dir*13, y, 12, 20, name));
        }

        realControls = controlList;
    }

    protected void actionPerformed(GuiButton guibutton)
    {
        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            return;
        }

        seedlibrary.sendGuiButton(guibutton.id, rightClick);
        super.actionPerformed(guibutton);
    }

    public void drawCenteredString(String s, int x, int y, int color) {
        fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s) / 2, y,
                                color);
    }

    public void drawRightString(String s, int x, int y, int color) {
        fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s), y,
                                color);
    }

    public void draw3DRect(int left, int top, int right, int bottom) {
        drawRect(left, top, right, bottom, 0xff373737);
        drawRect(left+1, top+1, right, bottom, 0xffffffff);
        drawRect(left+1, top+1, right-1, bottom-1, 0xffc6c6c6);
    }

    protected void drawGuiContainerForegroundLayer()
    {
        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
            return;
        }

        SeedLibraryFilter filter = seedlibrary.getGUIFilter();

        drawCenteredString("Seed Type", left + main_width / 4, top + 2,
                           0x404040);
        drawCenteredString(filter.getCropName(),
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

        String count;
        if (seedlibrary.seeds_available >= 65535) {
            count = "MANY";
        } else {
            count = seedlibrary.seeds_available + "";
        }
        drawCenteredString(count, 108, 88, 0x404040);
        drawCenteredString("Seeds", 108, 97, 0x404040);

        drawCenteredString("Allow unknown", left + main_width / 4, middle + 2,
                           0x404040);
        drawCenteredString("Type", left + main_width/8, middle + 11,
                           0x404040);
        drawCenteredString("GGR", left + (main_width*3)/8, middle + 11,
                           0x404040);

        try {
            int type_y = 54 + 1;
            if (filter.allow_unknown_type) {
                type_y = 72 + 1;
            }
            textureY.set(unk_type_button, type_y);
            int ggr_y = 54 + 1;
            if (filter.allow_unknown_ggr) {
                ggr_y = 72 + 1;
            }
            textureY.set(unk_ggr_button, ggr_y);
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        }

        if (!seedlibrary.hasEnergy()) {
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

            controlList = noControls;
        } else {
            controlList = realControls;
        }

        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);

        String tooltip = getTooltip(mouseX, mouseY);
        if (tooltip != null && tooltip.length() > 0) {
            showTooltip(mouseX, mouseY, tooltip);
        }

        super.drawGuiContainerForegroundLayer();
    }

    public String getTooltip(int x, int y) {
        int slider;
        if (current_slider != -1) {
            slider = current_slider;
        } else {
            slider = getSliderAt(x, y);
        }

        if (slider != -1) {
            int value = getSliderValue(slider);
            if (slider > 5) {
                value *= 3;
            }

            return getSliderName(slider) + WHITE + ": " + value;
        }

        return null;
    }

    public void showTooltip(int x, int y, String contents) {
        drawCreativeTabHoveringText(contents, x - guiLeft, y - guiTop);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            return;
        }

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

        /*
        // Draw the borders for the three upper sections.
        draw3DRect(left, top, center, middle);
        draw3DRect(left, middle, center, bottom);
        draw3DRect(center, top, right, bottom);
        */

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
        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            return;
        }

        if (!seedlibrary.hasEnergy()) {
            current_slider = -1;
            return;
        }

        if (button == 1)
        {
            // Pass the right click to the directional buttons.
            rightClick = true;
            for (int l = 0; l < controlList.size(); l++)
            {
                GuiButton guibutton = (GuiButton)controlList.get(l);
                if (guibutton.id < 4 || guibutton.id > 9) {
                    continue;
                }
                if (guibutton.mousePressed(mc, x, y))
                {
                    rightSelect = guibutton;
                    mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
                    actionPerformed(guibutton);
                }
            }
            rightClick = false;
        }

        if (button == 0) {
            // LMB down.

            // Set current slider to what's under the mouse, so it can track.
            current_slider = getSliderAt(x, y);

            // And if there is one, keep track of the starting point as well.
            if (current_slider != -1) {
                drag_start_x = x;
                drag_start_value = getSliderValue(current_slider);
            }
        }
    }


    public int getSliderAt(int x, int y) {
        // Adjust for GUI coordinates.
        x -= guiLeft;
        y -= guiTop;

        if (x < (sliders_x - 2) || y < sliders_y) {
            // Above or left of the bars.
            return -1;
        }

        x -= sliders_x;
        y -= sliders_y;

        int bar = y / sliders_spacing;
        int remainder = y % sliders_spacing;
        if (bar > 3 || remainder >= 10) {
            // Below or between the bars.
            return -1;
        }

        int min = getSliderValue(bar*2);
        int max = getSliderValue(bar*2 + 1);

        if (x < min * 2 - 2) {
            // Left of both arrows.
            return -1;
        } else if (x <= min * 2) {
            // Over the minimum arrow.
            return bar * 2;
        } else if (x < max * 2) {
            // Between the arrows.
            return -1;
        } else if (x <= max * 2 + 2) {
            // Over the maximum arrow;
            return bar * 2 + 1;
        } else {
            // Right of both arrows.
            return -1;
        }
    }

    public String getSliderName(int slider) {
        String name;
        int bar = slider / 2;
        int arrow = slider % 2;

        if (arrow == 0) {
            name = "Minimum ";
        } else {
            name = "Maximum ";
        }

        if (bar == 0) {
            name += DARK_GREEN + "Growth";
        } else if (bar == 1) {
            name += GOLD + "Gain";
        } else if (bar == 2) {
            name += AQUA + "Resistance";
        } else { // bar == 3
            name += YELLOW + "Total";
        }

        return name;
    }

    public int getSliderValue(int slider) {
        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            return 0;
        }

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
        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            return;
        }

        seedlibrary.sendGuiSlider(slider, value);
    }


    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);

        SeedLibraryTileEntity seedlibrary = getLibrary();
        if (seedlibrary == null) {
            return;
        }

        mouseX = x;
        mouseY = y;

        if (rightSelect != null && button == 1) {
            // Release a button pressed with RMB.
            rightSelect.mouseReleased(x, y);
            rightSelect = null;
        }

        if (!seedlibrary.hasEnergy()) {
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
