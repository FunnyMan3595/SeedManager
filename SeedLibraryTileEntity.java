import java.util.Random;
import net.minecraft.src.IInventory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.api.IWrenchable;

public class SeedLibraryTileEntity extends TileEntity implements IWrenchable, IInventory {
    private ItemStack shallowContents[];

    public SeedLibraryTileEntity() {
        super();
        shallowContents = new ItemStack[9];
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

    public ItemStack getStackInSlot(int i)
    {
        return shallowContents[i];
    }

    public ItemStack decrStackSize(int i, int j)
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

    public void setInventorySlotContents(int i, ItemStack itemstack)
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

}
