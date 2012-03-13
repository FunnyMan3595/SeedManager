import java.util.Random;
import java.util.HashMap;
import java.util.Vector;
import net.minecraft.src.IInventory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.ic2.platform.Platform;
import net.minecraft.src.ic2.common.TileEntityElecMachine;
import net.minecraft.src.ic2.common.ItemCropSeed;
import net.minecraft.src.ic2.api.CropCard;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.api.IWrenchable;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;

public class SeedLibraryTileEntity extends TileEntityElecMachine implements IWrenchable, ISpecialInventory {
    public static final boolean DEBUG_SEEDS = false;
    protected SeedLibraryFilter[] filters = new SeedLibraryFilter[7];
    protected HashMap<String, ItemStack> deepContents = new HashMap<String, ItemStack>();
    protected Vector<ItemStack> unresearched = new Vector<ItemStack>();

    public SeedLibraryTileEntity() {
        super(9, 0, 200, 32);

        for (int i=0; i<filters.length; i++) {
            filters[i] = new SeedLibraryFilter();
        }
    }

    public void updateEntity()
    {
        super.updateEntity();
        if (Platform.isSimulating())
        {
            if (energy > 0) {
                energy--;
            }
            provideEnergy();
        }
    }

    public void importFromInventory() {
        for (int i=0; i<9; i++) {
            if (SeedAnalyzerTileEntity.isSeed(inventory[i])) {
                storeSeeds(inventory[i]);
                inventory[i] = null;
            }
        }
    }

    public void exportToInventory() {
        for (int i=0; i<9; i++) {
            if (inventory[i] == null) {
                // Get a seed from the active filter.
                ItemStack seed = filters[6].getSeed(deepContents.values());

                if (seed == null) {
                    // No seeds left; stop exporting.
                    break;
                }

                // Add one of the seed to the inventory.
                inventory[i] = seed.copy();
                inventory[i].stackSize = 1;

                // And remove the seed from main storage.
                removeSeeds(inventory[i]);
            }
        }
    }

    public SeedLibraryFilter getGUIFilter() {
        return filters[6];
    }

    public int getGUISeedCount() {
        return filters[6].getCount(deepContents.values());
    }

    public ItemStack getResearchSeed() {
        int count = unresearched.size();
        if (count > 0) {
            ItemStack seed = unresearched.get(count - 1).copy();
            seed.stackSize = 1;

            removeSeeds(seed);

            return seed;
        }

        return null;
    }

    public void storeSeeds(ItemStack seeds) {
        String key = getKey(seeds);
        ItemStack stored = deepContents.get(key);
        if (stored != null) {
            // Found a pre-existing stack.  Using it will update everyone.
            stored.stackSize += seeds.stackSize;
        } else {
            // No pre-existing stack.  Make a new one.
            stored = seeds.copy();

            // If it's not fully scanned, prep it for analysis.
            if (ItemCropSeed.getScannedFromStack(stored) < 4) {
                unresearched.add(stored);
            }

            // Add it to the main storage bank.
            deepContents.put(key, stored);

            // Inform filters of the new seed.
            for (SeedLibraryFilter filter : filters) {
                filter.newSeed(stored);
            }
        }
    }

    public void removeSeeds(ItemStack seeds) {
        String key = getKey(seeds);
        ItemStack stored = deepContents.get(key);
        if (stored != null) {
            // Found a pre-existing stack, so we can reduce it.
            stored.stackSize -= seeds.stackSize;

            if (stored.stackSize <= 0) {
                // None left.

                // If it's not fully scanned, remove it from the analyser menu.
                if (ItemCropSeed.getScannedFromStack(stored) < 4) {
                    unresearched.remove(stored);
                }

                // Remove it from main storage.
                deepContents.remove(getKey(stored));

                // Inform filters that the seed isn't available anymore.
                for (SeedLibraryFilter filter : filters) {
                    filter.lostSeed(stored);
                }
            }
        }
    }

    // Save/load
    public void readFromNBT(NBTTagCompound input)
    {
        super.readFromNBT(input);
        deepContents.clear();
        unresearched.clear();

        NBTTagList filterlist = input.getTagList("Filters");
        for (int i = 0; i < 7; i++) {
            NBTTagCompound filter = (NBTTagCompound)filterlist.tagAt(i);
            filters[i].loadFromNBT(filter);
        }

        NBTTagList inventorytag = input.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < inventorytag.tagCount(); i++)
        {
            NBTTagCompound slot = (NBTTagCompound)inventorytag.tagAt(i);
            int j = slot.getByte("Slot");
            ItemStack stack = ItemStack.loadItemStackFromNBT(slot);
            if (j >= 0 && j < inventory.length)
            {
                inventory[j] = stack;
            } else {
                storeSeeds(stack);
            }
        }
    }

    public void writeToNBT(NBTTagCompound output)
    {
        super.writeToNBT(output);
        NBTTagList inventorytag = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            if (inventory[i] != null)
            {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(slot);
                inventorytag.appendTag(slot);
            }
        }

        for (ItemStack seed : deepContents.values()) {
            NBTTagCompound seedtag = new NBTTagCompound();
            seedtag.setByte("Slot", (byte) -1);
            seed.writeToNBT(seedtag);
            inventorytag.appendTag(seedtag);
        }

        output.setTag("Items", inventorytag);

        NBTTagList filterlist = new NBTTagList();
        for (int i = 0; i < 7; i++) {
            NBTTagCompound filtertag = new NBTTagCompound();
            filters[i].writeToNBT(filtertag);
            filterlist.appendTag(filtertag);
        }

        output.setTag("Filters", filterlist);
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
        return deepContents.isEmpty();
    }

    public float getWrenchDropRate() {
        return 1.0f;
    }


    // IInventory
    public synchronized ItemStack getStackInSlot(int slot)
    {
        if (slot < 0 || slot >= 9) {
            return null;
        }
        return super.getStackInSlot(slot);
    }

    public synchronized ItemStack decrStackSize(int i, int j)
    {
        if (i < 0 || i >= 9) {
            return null;
        }
        return super.decrStackSize(i, j);
    }

    public synchronized void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (i < 0 || i >= 9) {
            return;
        }
        super.setInventorySlotContents(i, itemstack);
    }

    public String getInvName()
    {
        return "Seed Library";
    }


    // ISpecialInventory
    public synchronized boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
        // When out of power, input continues to work.
        // (It's assumed that this is a simple operation, and that it's the
        //  analysis and sorting for extraction that takes power.)

        if (stack.itemID != Items.getItem("cropSeed").itemID) {
            return false;
        }

        if (!doAdd) {
            return true;
        }

        storeSeeds(stack);

        stack.stackSize = 0;

        return true;
    }

    public synchronized ItemStack extractItem(boolean doRemove, Orientations from) {
        // When out of power, output is disabled.
        if (energy <= 0) {
            return null;
        }

        if (DEBUG_SEEDS && from == Orientations.YPos) {
            Random rand = new Random();
            short id = (short) (rand.nextInt(15) + 1);
            byte growth = (byte) rand.nextInt(32);
            byte gain = (byte) rand.nextInt(32);
            byte resist = (byte) rand.nextInt(32);
            return ItemCropSeed.generateItemStackFromValues(id, growth, gain, resist, (byte)0);
        } else {
            int dir = from.ordinal();
            ItemStack stored = filters[dir].getSeed(deepContents.values());
            if (stored == null) {
                return null;
            }

            ItemStack extracted = stored.copy();
            extracted.stackSize = 1;
            if (doRemove) {
                removeSeeds(extracted);
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
