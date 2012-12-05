package org.ldg.seedmanager;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import ic2.common.TileEntityElectricMachine;
import ic2.common.ItemCropSeed;
import ic2.api.Items;
import ro.narc.liquiduu.IAcceleratorFriend;
import ro.narc.liquiduu.InstantRecipe;

public class SeedAnalyzerTileEntity extends TileEntityElectricMachine implements IHasFront, IAcceleratorFriend {
    public static final int[] cost_to_upgrade = {10, 90, 900, 9000};
    public static final int cost_reduction = 2;
    public int front = 3;

    // This keeps track of the analyzer's internal state.  When it changes, a
    // metadata change is forced (even if not needed) so that the graphics
    // update.
    public int state = -1;
    public SeedAnalyzerTileEntity()
    {
        super(3, 5, 2000/cost_reduction, 32);
    }

    @Override
    public int getFront() {
        return front;
    }

    public static boolean isSeed(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (stack.itemID != Items.getItem("cropSeed").itemID) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack getResultFor(ItemStack input, boolean reduce_stack) {
        if (!isSeed(input)) {
            return null;
        }

        ItemStack old_seed = input;

        short id = ItemCropSeed.getIdFromStack(old_seed);
        byte growth = ItemCropSeed.getGrowthFromStack(old_seed);
        byte gain = ItemCropSeed.getGainFromStack(old_seed);
        byte resistance = ItemCropSeed.getResistanceFromStack(old_seed);
        byte scan = ItemCropSeed.getScannedFromStack(old_seed);

        if (scan < 0) {
            scan = 0;
        }

        scan++;

        if (scan > 4) {
            scan = 4;
        }

        if (reduce_stack) {
            input.stackSize--;
        }

        return ItemCropSeed.generateItemStackFromValues(id, growth, gain, resistance, scan);
    }

    @Override
    public boolean canOperate() {
        if (isRedstonePowered()) {
            boolean need_input = (inventory[0] == null);
            boolean need_output = isSeed(inventory[2]);

            if (need_input && need_output) {
                if (ItemCropSeed.getScannedFromStack(inventory[2]) < 4) {
                    inventory[0] = inventory[2];
                    inventory[2] = null;
                    return true;
                }
            }

            for (int dir=0; dir<4; dir++) {
                if (!need_input && !need_output) {
                    break;
                }

                int x = xCoord;
                int y = yCoord;
                int z = zCoord;
                if (dir == 0) {
                    x++;
                } else if (dir == 1) {
                    x--;
                } else if (dir == 2) {
                    z++;
                } else {
                    z--;
                }

                TileEntity te = worldObj.getBlockTileEntity(x, y, z);
                if (te != null && te instanceof SeedLibraryTileEntity) {
                    SeedLibraryTileEntity library = (SeedLibraryTileEntity) te;
                    if (need_input && library.energy > 0) {
                        ItemStack seed = library.getResearchSeed();
                        if (seed != null) {
                            inventory[0] = seed;
                            need_input = false;
                        }
                    }

                    if (need_output) {
                        library.storeSeeds(inventory[2]);
                        inventory[2] = null;
                        need_output = false;
                    }
                }
            }
        }

        if (!isSeed(inventory[0])) {
            return false;
        }

        if (inventory[2] != null) {
            return false;
        }

        byte scan = ItemCropSeed.getScannedFromStack(inventory[0]);

        if (scan < 0) {
            scan = 0;
        }

        if (scan > 3) {
            return false;
        }

        defaultOperationLength = cost_to_upgrade[scan] /
                                 (defaultEnergyConsume * cost_reduction);
        return true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        updateState();
    }

    public void updateState() {
        int new_state = 0;
        if (energy > 0) {
            new_state += SeedManagerBlock.BIT_HAS_POWER;
        } 
        if (isSeed(inventory[0])) {
            new_state += SeedManagerBlock.BIT_HAS_SEED;
        }

        if (canOperate()) {
            // Will always have energy and a seed.
            new_state += SeedManagerBlock.BIT_WORKING;
        }

        if (new_state != state) {
            state = new_state;
            setMetadata();
        }
    }

    public void setMetadata() {
        setMetadata(SeedManagerBlock.DATA_ANALYZER + state);
    }

    public void setMetadata(int correctData) {

        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, correctData);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public String getInvName() {
        return "Seed Analyzer";
    }

    @Override
    public String getGuiClassName(EntityPlayer player) {
        return "SeedAnalyzerGUI";
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
        // Always drop the canonical item.
        setMetadata(SeedManagerBlock.DATA_ANALYZER_BLOCKED);

        return true;
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0f;
    }

    //public interface IAcceleratorFriend {
    // Is this machine ready to use this input?
    @Override
    public boolean instantReady(ItemStack input) {
        return true;
    }

    // What's the recipe for this input?
    @Override
    public InstantRecipe getInstantRecipe(ItemStack input) {
        if (!isSeed(input)) {
            return null;
        }

        byte scan = ItemCropSeed.getScannedFromStack(input);
        if (scan >= 4) {
            return new InstantRecipe(input, input, 0);
        }

        short id = ItemCropSeed.getIdFromStack(input);
        byte growth = ItemCropSeed.getGrowthFromStack(input);
        byte gain = ItemCropSeed.getGainFromStack(input);
        byte resistance = ItemCropSeed.getResistanceFromStack(input);
        ItemStack output = ItemCropSeed.generateItemStackFromValues(id, growth, gain, resistance, (byte)4);

        int cost = 450;
        if (scan < 3) {
            cost += 45;
        }
        if (scan < 2) {
            cost += 4;
        }
        if (scan < 1) {
            cost += 1;
        }

        return new InstantRecipe(input, output, cost);
    }

    // How many of these batches can the machine handle right now?
    // NOTE: Only called in advanced mode (recipe.machine == null)
    @Override
    public int instantCapacity(InstantRecipe recipe, int batches) {
        return batches;
    }

    // We have made some batches of the recipe.
    // NOTE: Only called in advanced mode (recipe.machine == null)
    @Override
    public void instantProcess(InstantRecipe recipe, int batches) { }
    //}
}
