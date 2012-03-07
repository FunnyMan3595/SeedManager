import net.minecraft.src.ItemBlock;

public class SeedManagerItem extends ItemBlock {
    public SeedManagerItem(int i) {
        super(i);
    }

    public int getMetadata(int damage)
    {
        return damage;
    }
}
