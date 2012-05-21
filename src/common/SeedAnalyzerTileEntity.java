import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.ic2.common.TileEntityElectricMachine;
import net.minecraft.src.ic2.common.ItemCropSeed;
import net.minecraft.src.ic2.api.Items;

public class SeedAnalyzerTileEntity extends TileEntityElectricMachine {
    public static final int[] cost_to_upgrade = {10, 90, 900, 9000};
    public static final int cost_reduction = 2;
    public SeedAnalyzerTileEntity()
    {
        super(3, 5, 2000/cost_reduction, 32);
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

    public ItemStack getResultFor(ItemStack input, boolean reduce_stack) {
        if (!isSeed(inventory[0])) {
            return null;
        }

        ItemStack old_seed = inventory[0];

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

    public String getInvName() {
        return "Seed Analyzer";
    }

    public String getGuiClassName(EntityPlayer player) {
        return "SeedAnalyzerGUI";
    }
}
