import java.util.Random;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.BlockContainer;
import java.util.ArrayList;
import net.minecraft.src.IInventory;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.common.ContainerElectricMachine;
import net.minecraft.src.ic2.common.IHasGui;
import net.minecraft.src.ic2.platform.Platform;

public class SeedManagerBlock extends BlockContainer implements ITextureProvider {
    private Random random = new Random();

    public SeedManagerBlock(int id) {
        super(id, 0, Material.iron);
        setHardness(5F);
        setResistance(10F);
        setStepSound(soundMetalFootstep);
        setBlockName("seedManager");
    }

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer)
    {
        if (entityplayer.isSneaking())
        {
            return false;
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

    public void onBlockRemoval(World world, int i, int j, int k)
    {
        IInventory inventory = (IInventory)world.getBlockTileEntity(i, j, k);
        if (inventory != null)
        {
            for (int l = 0; l < inventory.getSizeInventory(); l++)
            {
                ItemStack itemstack = inventory.getStackInSlot(l);
                if (itemstack == null)
                {
                    continue;
                }
                float f = random.nextFloat() * 0.8F + 0.1F;
                float f1 = random.nextFloat() * 0.8F + 0.1F;
                float f2 = random.nextFloat() * 0.8F + 0.1F;
                while (itemstack.stackSize > 0)
                {
                    int i1 = random.nextInt(21) + 10;
                    if (i1 > itemstack.stackSize)
                    {
                        i1 = itemstack.stackSize;
                    }
                    itemstack.stackSize -= i1;
                    EntityItem entityitem = new EntityItem(world, (float)i + f, (float)j + f1, (float)k + f2, new ItemStack(itemstack.itemID, i1, itemstack.getItemDamage()));
                    float f3 = 0.05F;
                    entityitem.motionX = (float)random.nextGaussian() * f3;
                    entityitem.motionY = (float)random.nextGaussian() * f3 + 0.2F;
                    entityitem.motionZ = (float)random.nextGaussian() * f3;
                    if (itemstack.hasTagCompound())
                    {
                        entityitem.item.setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                    }
                    world.spawnEntityInWorld(entityitem);
                }
            }
        }
        super.onBlockRemoval(world, i, j, k);
    }

    protected int damageDropped(int data) {
        return 0;
    }

    public void addCreativeItems(ArrayList arraylist)
    {
        arraylist.add(new ItemStack(this, 1, 0));
        arraylist.add(new ItemStack(this, 1, 1));
    }
}
