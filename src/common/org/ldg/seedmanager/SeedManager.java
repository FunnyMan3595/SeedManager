package org.ldg.seedmanager;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

import ic2.api.Ic2Recipes;
import ic2.api.Items;
import ic2.core.block.machine.ContainerElectricMachine;
import ic2.core.Ic2Items;
import ic2.core.IHasGui;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;

@Mod(
    modid="IC2.SeedManager",
    name="SeedManager",
    version="%conf:VERSION%",
    dependencies="required-after:IC2"
)
@NetworkMod(
    clientSideRequired=true,
    serverSideRequired=false,
    tinyPacketHandler=SeedManager.PacketHandler.class,
    versionBounds="%conf:VERSION_BOUNDS%"
)
public class SeedManager {

    public static final boolean DEBUG = false;
    public static Configuration config;
    public static SeedManager instance = null;
    public SeedManagerBlock seedmanager = null;

    public Class<? extends SeedAnalyzerTileEntity> analyzerClass = null;
    public Class<? extends SeedLibraryTileEntity> libraryClass = null;

    @SidedProxy(clientSide = "org.ldg.seedmanager.ClientProxy", serverSide = "org.ldg.seedmanager.CommonProxy")
    public static CommonProxy proxy;

    public static int real_mod(int number, int modulus) {
        int mod = number % modulus;
        if (mod < 0) {
            // Java is a fucking idiot.
            mod += modulus;
        }

        return mod;
    }

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

        // Register with the block registry.
        seedmanager = new SeedManagerBlock(ids[0]);
        GameRegistry.registerBlock(seedmanager, SeedManagerItem.class, seedmanager.getBlockName());

        // Initialize analyzerClass and libraryClass.
        initClasses();
        GameRegistry.registerTileEntity(analyzerClass, "Seed Analyzer");
        GameRegistry.registerTileEntity( libraryClass, "Seed Library");

        // Overwrite the IC2 crop seed with the improved version.
        Ic2Items.cropSeed = new ItemStack(new ExtendedCropSeed(Ic2Items.cropSeed));

        // Item stacks for identifying the analyzer/library
        ItemStack seedAnalyzer = new ItemStack(seedmanager, 1, SeedManagerBlock.DATA_ANALYZER_BLOCKED);
        ItemStack seedLibrary = new ItemStack(seedmanager, 1, SeedManagerBlock.DATA_LIBRARY_ON);

        proxy.init(seedmanager, seedAnalyzer, seedLibrary);

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

    @SuppressWarnings("unchecked")
    public void initClasses() {
        try {
            // Bind to LiquidUU if available.
            Class analyzerWithUU = Class.forName("org.ldg.seedmanager.SeedAnalyzerTileEntityUU");
            analyzerClass = (Class<? extends SeedAnalyzerTileEntity>) analyzerWithUU;
        } catch (Exception e) {
            // Otherwise, use the standard version.
            analyzerClass = SeedAnalyzerTileEntity.class;
        }
        try {
            // Bind to Buildcraft if available.
            Class libraryWithBC = Class.forName("org.ldg.seedmanager.SeedLibraryTileEntityBC");
            libraryClass = (Class<? extends SeedLibraryTileEntity>) libraryWithBC;
        } catch (Exception e) {
            // Otherwise, use the standard version.
            libraryClass = SeedLibraryTileEntity.class;
        }
    }

    public static Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public static SeedManager instance() {
        return instance;
    }

    public static class PacketHandler implements ITinyPacketHandler {
        @Override
        public void handle(NetHandler handler, Packet131MapData packet) {
            short id = packet.uniqueID;
            byte[] data = packet.itemData;

            if (DEBUG) {
                System.out.println("Got packet " + id + ":");
                String data_str = "";
                for (byte datum : data) {
                    data_str += String.format("%02x ", datum & 0xFF);
                }
                System.out.println(data_str);
            }

            if (SeedManager.getSide() == Side.CLIENT) {
                // XXX: Current packets assume a relevant seed library.
                //      This will need fixing if a new packet type is added.

                EntityPlayer player = proxy.getLocalPlayer();

                int x = MathHelper.floor_double(player.posX);
                int y = MathHelper.floor_double(player.posY);
                int z = MathHelper.floor_double(player.posZ);

                int x_end = data[0] & 0xff;
                int y_end = data[1] & 0xff;
                int z_end = data[2] & 0xff;

                if (DEBUG) {
                    System.out.println(String.format("X: %x - %x + %x = %x", x, (x % 256), data[0] & 0xff, x + x_end - real_mod(x, 256)));
                    System.out.println(String.format("Y: %x - %x + %x = %x", y, (y % 256), data[1] & 0xff, y + y_end - real_mod(y, 256)));
                    System.out.println(String.format("Z: %x - %x + %x = %x", z, (z % 256), data[2] & 0xff, z + z_end - real_mod(z, 256)));
                }

                // Clobber the last few bits of x,y,z with the values we got
                // in from the packet.
                x += x_end - real_mod(x, 256);
                y += y_end - real_mod(y, 256);
                z += z_end - real_mod(z, 256);

                TileEntity te = player.worldObj.getBlockTileEntity(x,y,z);

                SeedLibraryTileEntity seedLibrary = null;
                if (te instanceof SeedLibraryTileEntity) {
                    seedLibrary = (SeedLibraryTileEntity) te;
                } else {
                    System.out.println("Seed Library packet recieved, but missing or incompatible tile entity found: " + seedLibrary);
                    return;
                }

                if (id == 0) { // How many seeds does the filter match?
                    int seed_count = data[3] & 0xff;
                    seed_count += (data[4] & 0xff) * 256;

                    seedLibrary.seeds_available = seed_count;
                } else if (id == 1) { // What's the current filter?
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
                    if (!(player.openContainer instanceof SeedLibraryContainer)) {
                        return;
                    }

                    SeedLibraryContainer container = (SeedLibraryContainer)player.openContainer;

                    if (container.seedlibrary.energy <= 0) {
                        return;
                    }

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
