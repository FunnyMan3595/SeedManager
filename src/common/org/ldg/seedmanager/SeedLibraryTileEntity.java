package org.ldg.seedmanager;

import buildcraft.api.inventory.ISpecialInventory;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.Side;
import ic2.api.CropCard;
import ic2.api.Items;
import ic2.api.IWrenchable;
import ic2.common.IC2;
import ic2.common.ItemCropSeed;
import ic2.common.TileEntityElecMachine;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet54PlayNoteBlock;
import net.minecraft.src.TileEntity;

public class SeedLibraryTileEntity extends TileEntityElecMachine implements IWrenchable, ISpecialInventory {
    public static final boolean DEBUG_SEEDS = false;
    protected SeedLibraryFilter[] filters = new SeedLibraryFilter[7];
    protected HashMap<String, ItemStack> deepContents = new HashMap<String, ItemStack>();
    protected Vector<ItemStack> unresearched = new Vector<ItemStack>();

    // The number of seeds that match the GUI filter.
    public int seeds_available = 0;

    public int front = 3;

    public SeedLibraryTileEntity() {
        super(9, 0, 200, 32);

        for (int i=0; i<filters.length-1; i++) {
            filters[i] = new SeedLibraryFilter(null);
        }

        // The GUI filter gets a reference to the library, so that it can
        // announce when its count changes.
        filters[filters.length - 1] = new SeedLibraryFilter(this);
    }

    public Packet getDescriptionPacket() {
        return new Packet54PlayNoteBlock(xCoord, yCoord, zCoord, SeedManager.instance.seedmanager.blockID,
                                         0, front);
    }

    public void sendPacketToNearby(int id, byte[] data) {
        // Yes, this is unfriendly.
        // But everyone would have to do it themselves otherwise.
        data[0] = (byte) (xCoord & 0xff);
        data[1] = (byte) (yCoord & 0xff);
        data[2] = (byte) (zCoord & 0xff);

        if (worldObj == null) {
            return; // Inventory has no world.
        }

        Packet packet = PacketDispatcher.getTinyPacket(SeedManager.instance(),
                                                       (short)id, data);

        int dimension = worldObj.getWorldInfo().getDimension();
        PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 10,
                                               dimension, packet);
    }

    public void sendPacketToServer(int id, byte[] data) {
        Packet packet = PacketDispatcher.getTinyPacket(SeedManager.instance(),
                                                       (short)id, data);

        PacketDispatcher.sendPacketToServer(packet);
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

        // We only need to do the rest on the server side.
        if(SeedManager.getSide() == Side.CLIENT) {
            return;
        }

        // Notify all nearby players that the seed count has changed.
        byte[] data = new byte[5];

        if (new_count > 65535) {
            new_count = 65535;
        }

        data[3] = (byte) (new_count % 256);
        data[4] = (byte) (new_count / 256);

        sendPacketToNearby(0, data);
    }

    public void updateGUIFilter() {
        // We only need to do this on the server side.
        if(SeedManager.getSide() == Side.CLIENT) {
            return;
        }

        onInventoryChanged();

        // Notify all nearby players that the GUI filter has changed.
        NBTTagCompound nbt = new NBTTagCompound();

        getGUIFilter().writeToNBT(nbt);

        byte[] data;

        try {
            ByteArrayOutputStream byter = new ByteArrayOutputStream();

            // Pad it for the coordinates that will be added later.
            byter.write(0);
            byter.write(0);
            byter.write(0);

            GZIPOutputStream zipper = new GZIPOutputStream(byter);
            DataOutputStream out = new DataOutputStream(zipper);
            NBTBase.writeNamedTag(nbt, out);
            out.close();

            data = byter.toByteArray();
        } catch (IOException e) {
            return;
        }

        sendPacketToNearby(1, data);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        checkMetadata();
        if (energy > 0) {
            energy--;
        }
        if (energy < maxEnergy) {
            provideEnergy();
        }
    }

    public boolean hasEnergy() {
        return (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) 
                == SeedManagerBlock.DATA_LIBRARY_ON);
    }

    public void checkMetadata() {
        if (energy > 0) {
            checkMetadata(SeedManagerBlock.DATA_LIBRARY_ON);
        } else {
            checkMetadata(SeedManagerBlock.DATA_LIBRARY_OFF);
        }
    }

    public void checkMetadata(int correctData) {
        if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) != correctData) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, correctData);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

    public void sendGuiButton(int button, boolean rightClick) {
        byte[] data = new byte[2];
        data[0] = (byte) button;
        data[1] = (byte) (rightClick ? 1 : 0);

        sendPacketToServer(0, data);
    }

    public void receiveGuiButton(int button, boolean rightClick) {
        if (button == 0) {
            importFromInventory();
        } else if (button == 1) {
            exportToInventory();
        } else if (button == 2) {
            SeedLibraryFilter filter = getGUIFilter();
            filter.unknown_type = (filter.unknown_type + 1) % 3;
            filter.settingsChanged();
        } else if (button == 3) {
            SeedLibraryFilter filter = getGUIFilter();
            filter.unknown_ggr = (filter.unknown_ggr + 1) % 3;
            filter.settingsChanged();
        } else if (button < 10) {
            int dir = button - 4;
            if (rightClick) {
                filters[dir].copyFrom(filters[6]);
                onInventoryChanged();
            } else {
                filters[6].copyFrom(filters[dir]);
            }
        }
    }

    public void sendGuiSlider(int slider, int value) {
        byte[] data = new byte[2];
        data[0] = (byte) slider;
        data[1] = (byte) value;

        sendPacketToServer(1, data);
    }

    public void receiveGuiSlider(int slider, int value) {
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
        onInventoryChanged();
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
            onInventoryChanged();
        }
    }

    // Save/load
    @Override
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

        if (input.hasKey("Facing")) {
            front = input.getInteger("Facing");
        }
    }

    @Override
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
        output.setInteger("Facing", front);
    }


    // public interface IWrenchable {
    @Override
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int facing) {
        return front != facing && facing > 1;
    }

    @Override
    public short getFacing() {
        return (short) front;
    }

    @Override
    public void setFacing(short facing) {
        front = facing;

        if (SeedManager.getSide() != Side.CLIENT) {
            worldObj.addBlockEvent(xCoord, yCoord, zCoord, SeedManager.instance.seedmanager.blockID,
                                   0, front);
        }
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
        if (deepContents.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0f;
    }
    // }


    // IInventory
    @Override
    public synchronized ItemStack getStackInSlot(int slot)
    {
        if (slot < 0 || slot >= 9) {
            return null;
        }
        return super.getStackInSlot(slot);
    }

    @Override
    public synchronized ItemStack decrStackSize(int i, int j)
    {
        if (i < 0 || i >= 9) {
            return null;
        }
        return super.decrStackSize(i, j);
    }

    @Override
    public synchronized void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (i < 0 || i >= 9) {
            return;
        }
        super.setInventorySlotContents(i, itemstack);
    }

    @Override
    public String getInvName()
    {
        return "Seed Library";
    }


    // ISpecialInventory
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
