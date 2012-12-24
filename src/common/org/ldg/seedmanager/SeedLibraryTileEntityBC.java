package org.ldg.seedmanager;

import buildcraft.api.inventory.ISpecialInventory;

import ic2.api.Items;
import ic2.core.item.ItemCropSeed;

import java.util.*;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.ForgeDirection;

public class SeedLibraryTileEntityBC extends SeedLibraryTileEntity implements ISpecialInventory {
    // public interface ISpecialInventory {
    @Override
    public synchronized int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
        // When out of power, input continues to work.
        // (It's assumed that this is a simple operation, and that it's the
        //  analysis and sorting for extraction that takes power.)

        if (stack.itemID != Items.getItem("cropSeed").itemID) {
            return 0;
        }

        if (doAdd) {
            storeSeeds(stack);
        }

        return stack.stackSize;
    }

    @Override
    public synchronized ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount) {
        // When out of power, output is disabled.
        if (energy <= 0) {
            return new ItemStack[0];
        }

        if (maxItemCount < 1) {
            return new ItemStack[0];
        }

        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if (DEBUG_SEEDS && from == ForgeDirection.UP) {
            Random rand = new Random();
            short id = (short) (rand.nextInt(15) + 1);
            byte growth = (byte) rand.nextInt(32);
            byte gain = (byte) rand.nextInt(32);
            byte resist = (byte) rand.nextInt(32);
            stacks.add(ItemCropSeed.generateItemStackFromValues(id, growth, gain, resist, (byte)0));
        } else {
            int dir = from.ordinal();
            ItemStack stored = filters[dir].getSeed(deepContents.values());
            if (stored != null) {
                int count = Math.min(stored.stackSize, maxItemCount);

                for (int i=0; i<count; i++) {
                    ItemStack extracted = stored.copy();
                    extracted.stackSize = 1;
                    if (doRemove) {
                        removeSeeds(extracted);
                    }

                    stacks.add(extracted);
                }
            }
        }

        return stacks.toArray(new ItemStack[0]);
    }
    // }
}
