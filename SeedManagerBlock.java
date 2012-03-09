import java.util.Random;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.ic2.api.Items;
import ic2.common.ContainerElectricMachine;
import ic2.common.IHasGui;
import ic2.platform.Platform;

public class SeedManagerBlock extends BlockContainer implements ITextureProvider {
    public SeedManagerBlock(int id) {
        super(id, 0, Material.iron);
        setHardness(5F);
        setResistance(10F);
        setStepSound(soundMetalFootstep);
        setBlockName("seedManager");
    }

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer)
    {
        if (world.isRemote)
        {
            return true;
        }
        TileEntity seedmanager = world.getBlockTileEntity(i, j, k);
        if (seedmanager != null)
        {
            if (seedmanager instanceof SeedLibraryTileEntity) {
                ModLoader.getMinecraftInstance().displayGuiScreen(new SeedLibraryGUI(entityplayer.inventory, seedmanager));
            } else if (seedmanager instanceof SeedAnalyzerTileEntity) {
                ModLoader.getMinecraftInstance().displayGuiScreen(new SeedAnalyzerGUI((ContainerElectricMachine) ((IHasGui)seedmanager).getGuiContainer(entityplayer)));
            }
        }
        return true;
    }

    public TileEntity getBlockEntity() {
        return null;
    }

    public TileEntity getBlockEntity(int data) {
        if (data == 0) {
            return new SeedAnalyzerTileEntity();
        } else { // data == 1
            return new SeedLibraryTileEntity();
        }
    }

    public int getBlockTextureFromSideAndMetadata(int side, int data) {
        return data;
    }

    public String getTextureFile() {
        return "/fm_seedmanager.png";
    }

    public int idDropped(int i, Random random, int j) {
        return Items.getItem("machine").itemID;
    }

    protected int damageDropped(int data) {
        return 0;
    }
}
