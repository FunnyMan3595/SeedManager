import net.minecraft.src.ItemStack;
import net.minecraft.src.ic2.common.ItemCropSeed;

public class SeedLibrarySort implements java.util.Comparator<ItemStack> {
    public static final int NO_SORT = -1;
    public static final int GROWTH = 0;
    public static final int GAIN = 1;
    public static final int RESISTANCE = 2;
    public static final int TOTAL = 3;

    public static final SeedLibrarySort NONE = new SeedLibrarySort(NO_SORT, false);
    public static final SeedLibrarySort GROWTH_ASC = new SeedLibrarySort(GROWTH, false);
    public static final SeedLibrarySort GROWTH_DESC = new SeedLibrarySort(GROWTH, true);
    public static final SeedLibrarySort GAIN_ASC = new SeedLibrarySort(GAIN, false);
    public static final SeedLibrarySort GAIN_DESC = new SeedLibrarySort(GAIN, true);
    public static final SeedLibrarySort RESISTANCE_ASC = new SeedLibrarySort(RESISTANCE, false);
    public static final SeedLibrarySort RESISTANCE_DESC = new SeedLibrarySort(RESISTANCE, true);
    public static final SeedLibrarySort TOTAL_ASC = new SeedLibrarySort(TOTAL, false);
    public static final SeedLibrarySort TOTAL_DESC = new SeedLibrarySort(TOTAL, true);

    public final int sort_type;
    public final boolean descending;

    public SeedLibrarySort(int type, boolean desc) {
        sort_type = type;
        descending = desc;
    }

    public int compare(ItemStack seed_1, ItemStack seed_2) {
        if (sort_type == NO_SORT) {
            return 0;
        }

        int value_1, value_2;
        if (sort_type == GROWTH) {
            value_1 = ItemCropSeed.getGrowthFromStack(seed_1);
            value_2 = ItemCropSeed.getGrowthFromStack(seed_2);
        } else if (sort_type == GAIN) {
            value_1 = ItemCropSeed.getGainFromStack(seed_1);
            value_2 = ItemCropSeed.getGainFromStack(seed_2);
        } else if (sort_type == RESISTANCE) {
            value_1 = ItemCropSeed.getResistanceFromStack(seed_1);
            value_2 = ItemCropSeed.getResistanceFromStack(seed_2);
        } else { //if (sort_type == TOTAL) {
            value_1 =   ItemCropSeed.getGrowthFromStack(seed_1)
                      + ItemCropSeed.getGainFromStack(seed_1)
                      + ItemCropSeed.getResistanceFromStack(seed_1);
            value_2 =   ItemCropSeed.getGrowthFromStack(seed_2)
                      + ItemCropSeed.getGainFromStack(seed_2)
                      + ItemCropSeed.getResistanceFromStack(seed_2);
        }

        if (descending) {
            return value_1 - value_2;
        } else {
            return value_2 - value_1;
        }
    }
}
