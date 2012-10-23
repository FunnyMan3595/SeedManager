import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.Side;
import ic2.api.Items;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class SeedManagerBlock extends BlockContainer {
    // Which side should get the front texture if we don't know?
    public static final int DEFAULT_FRONT_SIDE = 3;

    // An uninteresting side (i.e. not front, top, or bottom).
    public static final int DEFAULT_NON_FRONT_SIDE = 2;

    public static final byte DATA_ANALYZER_OFF = 0;
    public static final byte DATA_LIBRARY_OFF = 1;

    public static final byte DATA_ANALYZER_ON = 9;
    public static final byte DATA_LIBRARY_ON = 10;

    public Random random = new Random();

    public SeedManagerBlock(int id) {
        super(id, 0, Material.iron);
        setHardness(5F);
        setResistance(10F);
        setStepSound(soundMetalFootstep);
        setBlockName("seedManager");
    }

    public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float hit_x, float hit_y, float hit_z)
    {
        if (player.isSneaking())
        {
            return false;
        }

        TileEntity te = world.getBlockTileEntity(i, j, k);
        if (SeedManager.getSide() == Side.CLIENT) {
            return true; // Client bails out here.
        }

        if (te instanceof SeedLibraryTileEntity) {
            SeedLibraryTileEntity library = (SeedLibraryTileEntity) te;
            byte[] data = new byte[4];
            data[0] = (byte) library.xCoord;
            data[1] = (byte) library.yCoord;
            data[2] = (byte) library.zCoord;
            data[3] = (byte) (library.energy > 0 ? 1 : 0);

            Packet packet = PacketDispatcher.getTinyPacket(SeedManager.instance(), (short)0, data);
            PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
        }

        player.openGui(SeedManager.instance(), 0, world, i, j, k);
        return true;
    }

    public TileEntity createNewTileEntity(World world) {
        return null;
    }

    public TileEntity createNewTileEntity(World world, int data) {
        if (data == DATA_ANALYZER_OFF || data == DATA_ANALYZER_ON) {
            return new SeedAnalyzerTileEntity();
        } else if (data == DATA_LIBRARY_OFF || data == DATA_LIBRARY_ON) {
            return new SeedLibraryTileEntity();
        } else {
            return null;
        }
    }

    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     * YNeg=0
     * YPos=1
     * ZNeg=2
     * ZPos=3
     * XNeg=4
     * XPos=5
     */
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        int row = 0;
        int col = 0;

        TileEntity te = world.getBlockTileEntity(x,y,z);

        if (te instanceof SeedAnalyzerTileEntity) {
            SeedAnalyzerTileEntity analyzer = (SeedAnalyzerTileEntity) te;

            if (side == 1) {
                row = 2;
                if (analyzer.isSeed(analyzer.inventory[0])) {
                    col = 1;
                }
            } else if (side == 0) {
                row = 3;
            } else if (side == analyzer.front) {
                if (analyzer.energy > 0) {
                    if (analyzer.canOperate()) {
                        col = 2;
                    } else {
                        col = 1;
                    }
                }
            } else {
                row = 1;
            }

            int tex = row*16 + col;
            return tex;
        } else if (te instanceof SeedLibraryTileEntity) {
            SeedLibraryTileEntity library = (SeedLibraryTileEntity) te;

            // For the library, we just fix the side index to match the default
            // front side, then fall through to the metadata-based version.
            if (side > 1) { // Not top or bottom.
                if (side == library.front) {
                    side = DEFAULT_FRONT_SIDE;
                } else {
                    side = DEFAULT_NON_FRONT_SIDE;
                }
            }
        }

        return getBlockTextureFromSideAndMetadata(side, world.getBlockMetadata(x, y, z));
    }

    public int getBlockTextureFromSideAndMetadata(int side, int data) {
        int x;
        int y;

        if (data == DATA_ANALYZER_OFF) {
            x = 0;
            y = 0;
        } else if (data == DATA_ANALYZER_ON) {
            x = 1;
            y = 0;
        } else if (data == DATA_LIBRARY_OFF) {
            x = 0;
            y = 4;
        } else { // data == DATA_LIBRARY_ON
            x = 1;
            y = 4;
        }

        if (side == 0) {
            y += 3;
        } else if (side == 1) {
            y += 2;
        } else if (side == DEFAULT_FRONT_SIDE) {
            y += 0;
        } else {
            y += 1;
        }

        return y*16 + x;
    }

    public String getTextureFile() {
        return "/fm_seedmanager.png";
    }

    public int idDropped(int i, Random random, int j) {
        return Items.getItem("machine").itemID;
    }

    public void breakBlock(World world, int x, int y, int z, int id, int data)
    {
        IInventory inventory = (IInventory)world.getBlockTileEntity(x, y, z);
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
                    EntityItem entityitem = new EntityItem(world, (float)x + f, (float)y + f1, (float)z + f2, new ItemStack(itemstack.itemID, i1, itemstack.getItemDamage()));
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
        super.breakBlock(world, x, y, z, id, data);
    }

    protected int damageDropped(int data) {
        return 0;
    }

    public void addCreativeItems(ArrayList arraylist)
    {
        arraylist.add(new ItemStack(this, 1, DATA_ANALYZER_OFF));
        arraylist.add(new ItemStack(this, 1, DATA_LIBRARY_OFF));
    }
}
