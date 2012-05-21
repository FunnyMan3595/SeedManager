import net.minecraft.src.ic2.common.ContainerElectricMachine;
import net.minecraft.src.ic2.common.TileEntityElectricMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class SeedAnalyzerGUI extends GuiContainer
{
    public ContainerElectricMachine container;

    public SeedAnalyzerGUI(ContainerElectricMachine containerelectricmachine)
    {
        super(containerelectricmachine);
        container = containerelectricmachine;
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Seed Analyzer", 58, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        int k = mc.renderEngine.getTexture("/ic2/sprites/GUICompressor.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        int j1 = (int)(14F * container.tileEntity.getChargeLevel());
        int k1 = (int)(24F * container.tileEntity.getProgress());
        if (j1 > 0)
        {
            drawTexturedModalRect(l + 56, (i1 + 36 + 14) - j1, 176, 14 - j1, 14, j1);
        }
        if (k1 > 0)
        {
            drawTexturedModalRect(l + 79, i1 + 34, 176, 14, k1 + 1, 16);
        }
    }
}
