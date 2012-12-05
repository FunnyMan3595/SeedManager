package org.ldg.seedmanager;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import java.util.List;

public class SeedLibraryContainer extends Container
{
    public SeedLibraryTileEntity seedlibrary;

    public SeedLibraryContainer(IInventory iinventory, SeedLibraryTileEntity seedmanager)
    {
        seedlibrary = seedmanager;

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(seedlibrary, i, 8 + i * 18, 108));
        }

        addSlotToContainer(new FakeSlot(seedlibrary, -1, 38, 16));

        int i = 2*18;

        for (int k = 0; k < 3; k++)
        {
            for (int j1 = 0; j1 < 9; j1++)
            {
                addSlotToContainer(new Slot(iinventory, j1 + k * 9 + 9, 8 + j1 * 18, 104 + k * 18 + i));
            }
        }

        for (int l = 0; l < 9; l++)
        {
            addSlotToContainer(new Slot(iinventory, l, 8 + l * 18, 162 + i));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return seedlibrary.isUseableByPlayer(entityplayer);
    }

    @Override
    public ItemStack slotClick(int i, int j, int k, EntityPlayer entityplayer) {
        if (i == 9) {
            // Clicked the "take a seed's type" slot.
            ItemStack seed = entityplayer.inventory.getItemStack();
            seedlibrary.getGUIFilter().setCropFromSeed(seed);
            return null;
        }
        return super.slotClick(i, j, k, entityplayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int i)
    {
        if (i == 10) {
            return null;
        }

        ItemStack itemstack = null;
        Slot slot = (Slot)inventorySlots.get(i);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (i < 9)
            {
                if (!mergeItemStack(itemstack1, 10, 46, true))
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
        }
        return itemstack;
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);

        seedlibrary.updateGUIFilter();
        seedlibrary.updateSeedCount();
    }
}
