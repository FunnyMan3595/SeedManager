import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.IInventory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Material;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.NetworkManager;
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

    public List listeners = null;

    // The number of seeds that match the GUI filter.
    public int seeds_available = 0;

    public SeedLibraryTileEntity() {
        super(9, 0, 200, 32);

        for (int i=0; i<filters.length-1; i++) {
            filters[i] = new SeedLibraryFilter(null);
        }

        // The GUI filter gets a reference to the library, so that it can
        // announce when its count changes.
        filters[filters.length - 1] = new SeedLibraryFilter(this);
    }

    public void updateCountIfMatch(ItemStack seed) {
        SeedLibraryFilter filter = getGUIFilter();
        if (!filter.bulk_mode && filter.isMatch(seed)) {
            updateSeedCount();
        }
    }

    public void updateSeedCount() {
        setSeedCount(getGUIFilter().getCount(deepContents.values()));
    }

    public void setSeedCount(int new_count) {
        seeds_available = new_count;

        // Notify all players with the library open that the seed count has
        // changed.
        if (listeners != null && !MinecraftForge.isClient()) {
            for (Object listener : listeners) {
                if (listener instanceof EntityPlayer) {
                    byte[] data = new byte[1];
                    if (new_count > 100) {
                        data[0] = (byte)100;
                    } else {
                        data[0] = (byte)new_count;
                    }
                    NetworkManager net = mod_SeedManager.instance.getNetManager((EntityPlayer)listener);

                    MinecraftForge.sendPacket(net, mod_SeedManager.instance,
                                              (short)1, data);
                }
            }
        }
    }

    public void updateGUIFilter() {
        // Notify all players with the library open that the GUI filter has
        // changed.
        if (listeners != null && !MinecraftForge.isClient()) {
            for (Object listener : listeners) {
                if (listener instanceof EntityPlayer) {
                    NBTTagCompound nbt = new NBTTagCompound();

                    getGUIFilter().writeToNBT(nbt);

                    byte[] data = new byte[0];

                    try {
                        ByteArrayOutputStream byter = new ByteArrayOutputStream();
                        GZIPOutputStream zipper = new GZIPOutputStream(byter);
                        DataOutputStream out = new DataOutputStream(zipper);
                        //DataOutputStream out = new DataOutputStream(byter);
                        NBTBase.writeNamedTag(nbt, out);
                        out.close();

                        data = byter.toByteArray();
                    } catch (IOException e) {}

                    NetworkManager net = mod_SeedManager.instance.getNetManager((EntityPlayer)listener);

                    MinecraftForge.sendPacket(net, mod_SeedManager.instance,
                                              (short)2, data);
                }
            }
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
            if (energy < maxEnergy) {
                provideEnergy();
            }
        }
    }

    public void importFromInventory() {
        getGUIFilter().bulk_mode = true;
        for (int i=0; i<9; i++) {
            if (SeedAnalyzerTileEntity.isSeed(inventory[i])) {
                storeSeeds(inventory[i]);
                inventory[i] = null;
            }
        }
        getGUIFilter().bulk_mode = false;
        updateSeedCount();
    }

    public void exportToInventory() {
        getGUIFilter().bulk_mode = true;
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
        getGUIFilter().bulk_mode = false;
        updateSeedCount();
    }

    public SeedLibraryFilter getGUIFilter() {
        return filters[6];
    }

    public int getGUISeedCount() {
        return filters[6].getCount(deepContents.values());
    }

    public void handleGuiButton(int button, boolean rightClick) {
        if (worldObj.isRemote) {
            byte[] data = new byte[2];
            data[0] = (byte) button;
            data[1] = (byte) (rightClick ? 1 : 0);

            NetworkManager net = mod_SeedManager.instance.getNetManager(null);
            MinecraftForge.sendPacket(net, mod_SeedManager.instance, (short)0,
                                      data);

            return;
        }

        if (button == 0) {
            importFromInventory();
        } else if (button == 1) {
            exportToInventory();
        } else if (button == 2) {
            SeedLibraryFilter filter = getGUIFilter();
            filter.allow_unknown_type = !filter.allow_unknown_type;
            filter.settingsChanged();
        } else if (button == 3) {
            SeedLibraryFilter filter = getGUIFilter();
            filter.allow_unknown_ggr = !filter.allow_unknown_ggr;
            filter.settingsChanged();
        } else if (button < 10) {
            int dir = button - 4;
            if (rightClick) {
                filters[dir].copyFrom(filters[6]);
            } else {
                filters[6].copyFrom(filters[dir]);
            }
        }
    }

    public void handleGuiSlider(int slider, int value) {
        if (worldObj.isRemote) {
            byte[] data = new byte[2];
            data[0] = (byte) slider;
            data[1] = (byte) value;

            NetworkManager net = mod_SeedManager.instance.getNetManager(null);
            MinecraftForge.sendPacket(net, mod_SeedManager.instance, (short)1,
                                      data);

            return;
        }

        SeedLibraryFilter filter = getGUIFilter();
        int bar = slider / 2;
        int arrow = slider % 2;
        if (bar == 0) {
            if (arrow == 0) {
                filter.min_growth = value;
            } else {
                filter.max_growth = value;
            }
        } else if (bar == 1) {
            if (arrow == 0) {
                filter.min_gain = value;
            } else {
                filter.max_gain = value;
            }
        } else if (bar == 2) {
            if (arrow == 0) {
                filter.min_resistance = value;
            } else {
                filter.max_resistance = value;
            }
        } else { // if (bar == 3)
            if (arrow == 0) {
                filter.min_total = value * 3;
            } else {
                filter.max_total = value * 3;
            }
        }

        filter.settingsChanged();
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
            // Found a pre-existing stack.  Using it will update everything...
            stored.stackSize += seeds.stackSize;

            // ...except the GUI's seed count, so update that now.
            updateCountIfMatch(stored);
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
            } else {
                // All we need to do is update the GUI count.
                updateCountIfMatch(stored);
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

        if (doAdd) {
            storeSeeds(stack);
            stack.stackSize = 0;
        }

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
