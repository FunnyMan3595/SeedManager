import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import java.util.List;

public class SeedLibraryContainer extends Container
{
    private SeedLibraryTileEntity seedmanager;

    public SeedLibraryContainer(IInventory iinventory, SeedLibraryTileEntity tileentitydispenser)
    {
        seedmanager = tileentitydispenser;

        for (int i = 0; i < 3; i++)
        {
            for (int l = 0; l < 3; l++)
            {
                addSlot(new Slot(tileentitydispenser, l + i * 3, 8 + l * 18, 18 + i * 18));
            }
        }

        int i = 2*18;

        for (int k = 0; k < 3; k++)
        {
            for (int j1 = 0; j1 < 9; j1++)
            {
                addSlot(new Slot(iinventory, j1 + k * 9 + 9, 8 + j1 * 18, 103 + k * 18 + i));
            }
        }

        for (int l = 0; l < 9; l++)
        {
            addSlot(new Slot(iinventory, l, 8 + l * 18, 161 + i));
        }
    }

    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return seedmanager.isUseableByPlayer(entityplayer);
    }

    public ItemStack transferStackInSlot(int i)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)inventorySlots.get(i);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (i < 9)
            {
                if (!mergeItemStack(itemstack1, 9, 45, true))
                {
                    return null;
                }
            }
            else if (!mergeItemStack(itemstack1, 0, 9, false))
            {
                return null;
            }
            if (itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }
            if (itemstack1.stackSize != itemstack.stackSize)
            {
                slot.onPickupFromSlot(itemstack1);
            }
            else
            {
                return null;
            }
        }
        return itemstack;
    }
}
