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
        int blockID = Integer.parseInt(config.getOrCreateBlockIdProperty("seed_manager", 190 ).value);
        config.save();

        // Preload the in-world texture.
        MinecraftForgeClient.preloadTexture("/fm_seedmanager.png");

        // Register with ModLoader.
        ModLoader.RegisterBlock(new SeedManagerBlock(blockID), SeedManagerItem.class);
        ModLoader.RegisterTileEntity(SeedAnalyzerTileEntity.class, "Seed Analyzer");
        ModLoader.RegisterTileEntity(SeedLibraryTileEntity.class, "Seed Library");
        ModLoader.AddLocalization("tile.seedAnalyzer.name", "Seed Analyzer");
        ModLoader.AddLocalization("tile.seedLibrary.name", "Seed Library");
        ModLoader.AddLocalization("tile.seedManager.name", "Seed Manager");

        // Overwrite the IC2 crop seed with the improved version.
        Ic2Items.cropSeed = new ItemStack(new VerboseItemCropSeed(Ic2Items.cropSeed));

        // Add recipes.
        ItemStack seedAnalyzer = new ItemStack(blockID, 1, 0);
        ItemStack seedLibrary = new ItemStack(blockID, 1, 1);

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
        return "1";
    }

    public String getPriorities() {
        return "required-after:mod_IC2;after:*";
    }
}
