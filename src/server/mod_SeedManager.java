import java.io.File;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ItemStack;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.ic2.api.Ic2Recipes;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.common.Ic2Items;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.ic2.common.ContainerElectricMachine;
import net.minecraft.src.ic2.common.IHasGui;
import net.minecraft.src.ic2.platform.Platform;

public class mod_SeedManager extends NetworkMod {
    public static final boolean DEBUG = false;
    public static Configuration config = new Configuration(new File("./config/SeedManager.cfg"));
    public static mod_SeedManager instance = null;

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
        // Client-side only.
        //MinecraftForgeClient.preloadTexture("/fm_seedmanager.png");

        // Register with ModLoader.
        SeedManagerBlock seedmanager = new SeedManagerBlock(ids[0]);
        ModLoader.registerBlock(seedmanager, SeedManagerItem.class);
        ModLoader.registerTileEntity(SeedAnalyzerTileEntity.class, "Seed Analyzer");
        ModLoader.registerTileEntity(SeedLibraryTileEntity.class, "Seed Library");
        //ModLoader.addLocalization("tile.seedAnalyzer.name", "Seed Analyzer");
        //ModLoader.addLocalization("tile.seedLibrary.name", "Seed Library");
        //ModLoader.addLocalization("tile.seedManager.name", "Seed Manager");

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

    public NetworkManager getNetManager(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            return ((EntityPlayerMP) player).playerNetServerHandler.netManager;
        }
        return null;
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

        EntityPlayerMP player = ((NetServerHandler)net.getNetHandler()).getPlayerEntity();
        if (id < 2) { // SeedLibrary functions
            if (!(player.craftingInventory instanceof SeedLibraryContainer)) {
                return;
            }

            SeedLibraryContainer container = (SeedLibraryContainer)player.craftingInventory;

            if (id == 0) { // GUI button clicked
                int button = data[0];
                boolean rightClick = (data[1] == 1);
                container.seedlibrary.handleGuiButton(button, rightClick);
            } else if (id == 1) { // Slider moved
                int slider = data[0];
                int value = data[1];
                container.seedlibrary.handleGuiSlider(slider, value);
            }
        }
    }

    public void setRemoteLibrary(SeedLibraryTileEntity library) {
        // Client only.
    }
}
