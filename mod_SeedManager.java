import net.minecraft.src.BaseMod;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ItemStack;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.ic2.common.Ic2Items;

public class mod_SeedManager extends BaseMod {

    public mod_SeedManager() {
    }

    public void load() {
        MinecraftForgeClient.preloadTexture("/fm_seedmanager.png");
        ModLoader.RegisterBlock(new SeedManagerBlock(190), SeedManagerItem.class);
        ModLoader.RegisterTileEntity(SeedLibraryTileEntity.class, "Seed Library");
        ModLoader.RegisterTileEntity(SeedAnalyzerTileEntity.class, "Seed Analyzer");
        ModLoader.AddLocalization("tile.seedManager.name", "Seed Manager");
        Ic2Items.cropSeed = new ItemStack(new VerboseItemCropSeed(Ic2Items.cropSeed));
    }

    public String getVersion() {
        return "1";
    }

    public String getPriorities() {
        return "required-after:mod_IC2";
    }
}
