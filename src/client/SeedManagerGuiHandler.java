import net.minecraft.src.forge.IGuiHandler;
import net.minecraft.src.ic2.common.ContainerElectricMachine;
import net.minecraft.src.ic2.common.IHasGui;
import net.minecraft.src.TileEntity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
public class SeedManagerGuiHandler implements IGuiHandler {
    public Object getGuiElement(int id, EntityPlayer player, World world,
                                int x, int y, int z) {
        TileEntity seedmanager = world.getBlockTileEntity(x, y, z);

        if (seedmanager instanceof SeedLibraryTileEntity) {
            return new SeedLibraryGUI(player.inventory, seedmanager);
        } else if (seedmanager instanceof SeedAnalyzerTileEntity) {
            return new SeedAnalyzerGUI((ContainerElectricMachine) ((IHasGui)seedmanager).getGuiContainer(player));
        }

        return null;
    }
}
