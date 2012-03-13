import net.minecraft.src.ic2.common.ItemCropSeed;
import net.minecraft.src.ic2.common.Ic2Items;
import net.minecraft.src.ItemStack;
import java.util.List;

public class VerboseItemCropSeed extends ItemCropSeed {
    public static final String yellow = '\247' + "e";
    public static final String green = '\247' + "a";
    public static final String black = '\247' + "0";

    public VerboseItemCropSeed(ItemStack parent) {
        super(parent.itemID-256, 152);
        setItemName("itemCropSeed");
    }

    public void addInformation(ItemStack stack, List list) {
        if (getScannedFromStack(stack) == 4) {
            int growth = getGrowthFromStack(stack);
            int gain = getGainFromStack(stack);
            int resistance = getResistanceFromStack(stack);
            list.add(green + "Gr " + pad(growth));
            list.add(green + "Ga " + pad(gain));
            list.add(green + "Re " + pad(resistance));
            list.add(yellow + "Lv " + (growth + gain + resistance));
        }
    }

    public String pad(int number) {
        if (number >= 10) {
            return "" + number;
        } else {
            return black + "0" + green + number;
        }
    }

}
