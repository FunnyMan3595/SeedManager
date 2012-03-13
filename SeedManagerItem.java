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
        if (i == 0) {
            return "tile.seedAnalyzer";
        } else if (i == 1) {
            return "tile.seedLibrary";
        } else {
            return "tile.seedManager";
        }
    }
}
