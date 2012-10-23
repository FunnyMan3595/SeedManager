import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import ic2.api.Ic2Recipes;
import ic2.api.Items;
import ic2.common.ContainerElectricMachine;
import ic2.common.Ic2Items;
import ic2.common.IHasGui;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet131MapData;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.TileEntity;

@Mod(
    modid="IC2.SeedManager",
    name="SeedManager",
    version="3.0",
    dependencies="required-after:IC2;after:*"
)
@NetworkMod(
    clientSideRequired=true,
    serverSideRequired=false,
    tinyPacketHandler=SeedManager.PacketHandler.class,
    versionBounds="[3.0]"
)
public class SeedManager {

    public static final boolean DEBUG = false;
    public static Configuration config;
    public static SeedManager instance = null;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;

        // Load config file.
        config = new Configuration(event.getSuggestedConfigurationFile());
    }

    @Mod.Init
    public void init(FMLInitializationEvent event) {
        Side side = getSide();

        try {
            config.load();
        } catch (RuntimeException e) {} // Just regenerate the config if it's
                                        // broken.
        String id_strs[] = {
            config.get("seed.manager", config.CATEGORY_BLOCK, 190).value,
        };
        config.save();

        int ids[] = new int[id_strs.length];
        try {
            for (int i=0; i<id_strs.length; i++) {
                ids[i] = Integer.parseInt(id_strs[i]);
            }
        } catch (NumberFormatException e) {
        }

        // Register with ModLoader.
        SeedManagerBlock seedmanager = new SeedManagerBlock(ids[0]);
        GameRegistry.registerBlock(seedmanager, SeedManagerItem.class);
        GameRegistry.registerTileEntity(SeedAnalyzerTileEntity.class, "Seed Analyzer");
        GameRegistry.registerTileEntity(SeedLibraryTileEntity.class, "Seed Library");

        // Overwrite the IC2 crop seed with the improved version.
        Ic2Items.cropSeed = new ItemStack(new ExtendedCropSeed(Ic2Items.cropSeed));

        // Item stacks for identifying the analyzer/library
        ItemStack seedAnalyzer = new ItemStack(seedmanager, 1, 0);
        ItemStack seedLibrary = new ItemStack(seedmanager, 1, 1);

        if (side == Side.CLIENT) {
            // Preload the in-world texture.
            MinecraftForgeClient.preloadTexture("/fm_seedmanager.png");

            // Set up the seed analyzer animation.
            TextureFXManager.instance().addAnimation(new SeedAnalyzerFX());

            // Add naming.
            LanguageRegistry.addName(seedmanager, "Seed Manager");
            LanguageRegistry.addName(seedAnalyzer, "Seed Analyzer");
            LanguageRegistry.addName(seedLibrary, "Seed Library");
        }

        // Add crafting recipes
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
        NetworkRegistry.instance().registerGuiHandler(this, new SeedManagerGuiHandler());
    }

    public static Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public static SeedManager instance() {
        return instance;
    }

    public static class PacketHandler implements ITinyPacketHandler {
        public void handle(NetHandler handler, Packet131MapData packet) {
            short id = packet.uniqueID;
            byte[] data = packet.itemData;

            if (DEBUG) {
                System.out.println("Got packet " + id + ":");
                String data_str = "";
                for (byte datum : data) {
                    data_str += datum + ",";
                }
                System.out.println(data_str);
            }

            if (SeedManager.getSide() == Side.CLIENT) {
                // XXX: Current packets assume a relevant seed library.
                //      This will need fixing if a new packet type is added.

                EntityPlayer player = Minecraft.getMinecraft().thePlayer;

                if (!player.worldObj.isRemote) {
                    return;
                }

                int x = MathHelper.floor_double(player.posX);
                int y = MathHelper.floor_double(player.posY);
                int z = MathHelper.floor_double(player.posZ);

                int x_end = data[0] & 0xff;
                int y_end = data[1] & 0xff;
                int z_end = data[2] & 0xff;

                // Clobber the last few bits of x,y,z with the values we got
                // in from the packet.
                x += x_end - (x % 256);
                y += y_end - (y % 256);
                z += z_end - (z % 256);

                TileEntity te = player.worldObj.getBlockTileEntity(x,y,z);

                SeedLibraryTileEntity seedLibrary = null;
                if (te instanceof SeedLibraryTileEntity) {
                    seedLibrary = (SeedLibraryTileEntity) te;
                } else {
                    return;
                }

                if (id == 0) { // Does the SeedLibrary have power?
                    seedLibrary.energy = data[3];
                } else if (id == 1) { // How many seeds does the filter match?
                    int seed_count = data[3] & 0xff;
                    seed_count += (data[4] & 0xff) * 256;

                    seedLibrary.seeds_available = seed_count;
                } else if (id == 2) { // What's the current filter?
                    NBTBase nbt = null;
                    try {
                        ByteArrayInputStream debyter = new ByteArrayInputStream(data);
                        // Strip off the 3 bytes of coordinates.
                        debyter.skip(3);

                        GZIPInputStream dezipper = new GZIPInputStream(debyter);
                        DataInputStream in = new DataInputStream(dezipper);
                        nbt = NBTBase.readNamedTag(in);
                    } catch (IOException e) {}

                    if (nbt instanceof NBTTagCompound) {
                        seedLibrary.getGUIFilter().loadFromNBT((NBTTagCompound)nbt);
                    }
                }
            } else { // Server/bukkit side
                EntityPlayerMP player = ((NetServerHandler)handler).getPlayer();
                if (id < 2) { // SeedLibrary functions
                    if (!(player.craftingInventory instanceof SeedLibraryContainer)) {
                        return;
                    }

                    SeedLibraryContainer container = (SeedLibraryContainer)player.craftingInventory;

                    if (id == 0) { // GUI button clicked
                        int button = data[0];
                        boolean rightClick = (data[1] == 1);
                        container.seedlibrary.receiveGuiButton(button, rightClick);
                    } else if (id == 1) { // Slider moved
                        int slider = data[0];
                        int value = data[1];
                        container.seedlibrary.receiveGuiSlider(slider, value);
                    }
                }
            }
        }
    }
}
