import java.io.File;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ItemStack;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.ic2.api.Ic2Recipes;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.common.Ic2Items;
import net.minecraft.src.forge.Configuration;

public class mod_SeedManager extends BaseMod {

    public static Configuration config = new Configuration(new File(Minecraft.getMinecraftDir(), "config/SeedManager.cfg"));

    public mod_SeedManager() {
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
    }

    public String getVersion() {
        return "v1.2.1";
    }

    public String getPriorities() {
        return "required-after:mod_IC2;after:*";
    }
}
