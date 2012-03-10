import java.util.Random;
import java.util.HashMap;
import net.minecraft.src.IInventory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.ic2.common.ItemCropSeed;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.api.IWrenchable;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;

public class SeedLibraryTileEntity extends TileEntity implements IWrenchable, ISpecialInventory {
    protected ItemStack shallowContents[];
    protected SeedLibraryFilter[] filters = new SeedLibraryFilter[7];
    protected HashMap<String, ItemStack> deepContents = new HashMap<String, ItemStack>();

    public SeedLibraryTileEntity() {
        super();
        shallowContents = new ItemStack[9];

        for (int i=0; i<filters.length; i++) {
            filters[i] = new SeedLibraryFilter();
        }

        filters[0].min_growth = 10;
        filters[0].min_gain = 10;
        filters[0].min_resistance = 10;
        filters[0].min_total = 40;
        filters[0].allow_unknown_ggr = false;
        filters[0].sort = SeedLibrarySort.TOTAL_DESC;
    }


    // Save/load
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        shallowContents = new ItemStack[getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xff;
            if (j >= 0 && j < shallowContents.length)
            {
                shallowContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < shallowContents.length; i++)
        {
            if (shallowContents[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                shallowContents[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
    }


    // IWrenchable
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side) {
        return false;
    }

    public short getFacing() {
        return 0;
    }

    public void setFacing(short facing) { }

    public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
        return true;
    }

    public float getWrenchDropRate() {
        return 1.0f;
    }


    // IInventory
    public int getSizeInventory()
    {
        return 9;
    }

    public synchronized ItemStack getStackInSlot(int i)
    {
        return shallowContents[i];
    }

    public synchronized ItemStack decrStackSize(int i, int j)
    {
        if (shallowContents[i] != null)
        {
            if (shallowContents[i].stackSize <= j)
            {
                ItemStack itemstack = shallowContents[i];
                shallowContents[i] = null;
                onInventoryChanged();
                return itemstack;
            }
            ItemStack itemstack1 = shallowContents[i].splitStack(j);
            if (shallowContents[i].stackSize == 0)
            {
                shallowContents[i] = null;
            }
            onInventoryChanged();
            return itemstack1;
        }
        else
        {
            return null;
        }
    }

    public synchronized void setInventorySlotContents(int i, ItemStack itemstack)
    {
        shallowContents[i] = itemstack;
        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
        onInventoryChanged();
    }

    public String getInvName()
    {
        return "Seed Library";
    }

    public int getInventoryStackLimit()
    {
        return 64;
    }

    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
        {
            return false;
        }
        return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
    }

    public void openChest()
    {
    }

    public void closeChest()
    {
    }


    // ISpecialInventory
    public synchronized boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
        if (stack.itemID != Items.getItem("cropSeed").itemID) {
            return false;
        }

        if (!doAdd) {
            return true;
        }

        // XXX If seeds stack >1 later, remove this line.
        stack.stackSize = 1;

        String key = getKey(stack);
        ItemStack stored = deepContents.get(key);
        if (stored != null) {
            stored.stackSize += stack.stackSize;
        } else {
            stored = stack.copy();
            deepContents.put(key, stored);
            for (SeedLibraryFilter filter : filters) {
                filter.newSeed(stored);
            }
        }

        stack.stackSize = 0;

        return true;
    }

    public synchronized ItemStack extractItem(boolean doRemove, Orientations from) {
        if (from == Orientations.YPos) {
            Random rand = new Random();
            short id = (short) (rand.nextInt(15) + 1);
            byte growth = (byte) rand.nextInt(32);
            byte gain = (byte) rand.nextInt(32);
            byte resist = (byte) rand.nextInt(32);
            return ItemCropSeed.generateItemStackFromValues(id, growth, gain, resist, (byte)4);
        } else {
            ItemStack stored = filters[0].getSeed(deepContents.values());
            if (stored == null) {
                return null;
            }

            ItemStack extracted = stored.copy();
            if (doRemove) {
                stored.stackSize--;
            }
            extracted.stackSize = 1;

            if (stored.stackSize <= 0) {
                deepContents.remove(getKey(stored));
                for (SeedLibraryFilter filter : filters) {
                    filter.lostSeed(stored);
                }
            }

            return extracted;
        }
    }


    // Deep inventory management.
    public String getKey(ItemStack seed) {
        short id = ItemCropSeed.getIdFromStack(seed);
        byte growth = ItemCropSeed.getGrowthFromStack(seed);
        byte gain = ItemCropSeed.getGainFromStack(seed);
        byte resistance = ItemCropSeed.getResistanceFromStack(seed);
        byte scan = ItemCropSeed.getScannedFromStack(seed);

        return id + ":" + growth + ":" + gain + ":" + resistance + ":" + scan;
    }

}
