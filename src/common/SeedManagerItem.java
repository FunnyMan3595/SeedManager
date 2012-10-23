import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemBlock;

public class SeedManagerItem extends ItemBlock {
    public SeedManagerItem(int i) {
        super(i);
        setHasSubtypes(true);
    }

    public int getMetadata(int damage)
    {
        return damage;
    }

    public String getItemNameIS(ItemStack itemstack)
    {
        int i = itemstack.getItemDamage();
        if (i >= SeedManagerBlock.DATA_ANALYZER) {
            return "tile.seedAnalyzer";
        } else if (   i == SeedManagerBlock.DATA_LIBRARY_OFF
            || i == SeedManagerBlock.DATA_LIBRARY_ON) {
            return "tile.seedLibrary";
        } else {
            return "tile.seedManager";
        }
    }
}
