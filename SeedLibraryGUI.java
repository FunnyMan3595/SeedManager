import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class SeedLibraryGUI extends GuiContainer
{
    public SeedLibraryGUI(InventoryPlayer inventoryplayer, TileEntity seedmanager)
    {
        super(new SeedLibraryContainer(inventoryplayer, (SeedLibraryTileEntity)seedmanager));
        ySize = ('\336' - 108) + 6*18;
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Seed Library", 8, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        int k = mc.renderEngine.getTexture("/fm_seedlibrary_gui.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, 6 * 18 + 17);
        drawTexturedModalRect(l, i1 + 6 * 18 + 17, 0, 126, xSize, 96);
    }
}
