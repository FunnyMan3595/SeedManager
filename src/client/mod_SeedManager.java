import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;
import java.io.DataInputStream;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ItemStack;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTBase;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.ic2.api.Ic2Recipes;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.common.Ic2Items;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.ic2.common.ContainerElectricMachine;
import net.minecraft.src.ic2.common.IHasGui;
import net.minecraft.src.ic2.platform.Platform;

public class mod_SeedManager extends NetworkMod implements IConnectionHandler {

    public static final boolean DEBUG = false;
    public static Configuration config = new Configuration(new File(Minecraft.getMinecraftDir(), "config/SeedManager.cfg"));
    public static mod_SeedManager instance = null;
    public NetworkManager netManager = null;
    public SeedLibraryTileEntity remoteSeedLibrary = null;

    public mod_SeedManager() {
        instance = this;
    }

    public void load() {
        // Get block ID from config.
        try {
            config.load();
        } catch (RuntimeException e) {} // Just regenerate the config if it's
                                        // broken.
        String id_strs[] = {
            config.getOrCreateIntProperty("seed.manager",
                                          config.CATEGORY_BLOCK, 190).value,
        };
        config.save();

        int ids[] = new int[id_strs.length];
        try {
            for (int i=0; i<id_strs.length; i++) {
                ids[i] = Integer.parseInt(id_strs[i]);
            }
        } catch (NumberFormatException e) {
        }

        // Preload the in-world texture.
        MinecraftForgeClient.preloadTexture("/fm_seedmanager.png");

        // Register with ModLoader.
        SeedManagerBlock seedmanager = new SeedManagerBlock(ids[0]);
        ModLoader.registerBlock(seedmanager, SeedManagerItem.class);
        ModLoader.registerTileEntity(SeedAnalyzerTileEntity.class, "Seed Analyzer");
        ModLoader.registerTileEntity(SeedLibraryTileEntity.class, "Seed Library");
        ModLoader.addLocalization("tile.seedAnalyzer.name", "Seed Analyzer");
        ModLoader.addLocalization("tile.seedLibrary.name", "Seed Library");
        ModLoader.addLocalization("tile.seedManager.name", "Seed Manager");

        // Overwrite the IC2 crop seed with the improved version.
        Ic2Items.cropSeed = new ItemStack(new VerboseItemCropSeed(Ic2Items.cropSeed));

        // Add recipes.
        ItemStack seedAnalyzer = new ItemStack(seedmanager, 1, 0);
        ItemStack seedLibrary = new ItemStack(seedmanager, 1, 1);

        Ic2Recipes.addCraftingRecipe(seedAnalyzer, new Object[] {
            " Z ", "#M#", "#C#",
            Character.valueOf('Z'), Items.getItem("cropnalyzer"),
            Character.valueOf('M'), Items.getItem("machine"),
            Character.valueOf('C'), Items.getItem("electronicCircuit"),
            Character.valueOf('S'), Item.seeds,
            Character.valueOf('#'), Block.planks,
        });
        Ic2Recipes.addCraftingRecipe(seedLibrary, new Object[] {
            "GGG", "#C#", "#Z#",
            Character.valueOf('Z'), seedAnalyzer,
            Character.valueOf('C'), Items.getItem("advancedCircuit"),
            Character.valueOf('G'), Block.glass,
            Character.valueOf('#'), Block.chest,
        });


        // Register the GUI handler.
        MinecraftForge.setGuiHandler(this, new SeedManagerGuiHandler());

        // Register as a connection handler, so we can trap NetworkManager.
        MinecraftForge.registerConnectionHandler(this);
    }

    public String getVersion() {
        return "v2.0";
    }

    public String getPriorities() {
        return "required-after:mod_IC2;after:*";
    }

    public boolean clientSideRequired() {
        return true;
    }

    public boolean serverSideRequired() {
        return false;
    }

    public void onConnect(NetworkManager network) { }

    public void onLogin(NetworkManager network, Packet1Login login) {
        netManager = network;
    }

    public void onDisconnect(NetworkManager network, String message, Object[] args) { }

    public NetworkManager getNetManager(EntityPlayer player) {
        return netManager;
    }

    public void onPacketData(NetworkManager net, short id, byte[] data) {
        if (DEBUG) {
            System.out.println("Got packet " + id + ":");
            String data_str = "";
            for (byte datum : data) {
                data_str += datum + ",";
            }
            System.out.println(data_str);
        }

        if (id == 0) { // Does the SeedLibrary have power?
            if (remoteSeedLibrary != null) {
                remoteSeedLibrary.energy = data[0];
            }
        } else if (id == 1) { // How many seeds does the filter match?
            if (remoteSeedLibrary != null) {
                remoteSeedLibrary.seeds_available = data[0];
            }
        } else if (id == 2) { // What's the current filter?
            if (remoteSeedLibrary != null) {
                NBTBase nbt = null;
                try {
                    ByteArrayInputStream debyter = new ByteArrayInputStream(data);
                    GZIPInputStream dezipper = new GZIPInputStream(debyter);
                    DataInputStream in = new DataInputStream(dezipper);
                    nbt = NBTBase.readNamedTag(in);
                } catch (IOException e) {}

                if (nbt instanceof NBTTagCompound) {
                    remoteSeedLibrary.getGUIFilter().loadFromNBT((NBTTagCompound)nbt);
                }
            }
        }
    }

    public void setRemoteLibrary(SeedLibraryTileEntity library) {
        remoteSeedLibrary = library;
    }
}
