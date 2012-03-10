import net.minecraft.src.ItemStack;
import net.minecraft.src.ic2.common.ItemCropSeed;
import java.util.Collection;
import java.util.Vector;

public class SeedLibraryFilter {
    public boolean allow_unknown_type = true;
    public boolean allow_unknown_ggr = true;
    public int seed_type = -1;
    public int min_growth = 0;
    public int min_gain = 0;
    public int min_resistance = 0;
    public int max_growth = 31;
    public int max_gain = 31;
    public int max_resistance = 31;
    public int min_total = 0;
    public int max_total = 93;
    public SeedLibrarySort sort = SeedLibrarySort.NONE;

    public static final int CACHE_SIZE = 10;
    public Vector<ItemStack> cache = new Vector<ItemStack>(CACHE_SIZE+1);
    public boolean cached_nothing = false;

    public ItemStack getSeed(Collection<ItemStack> seeds) {
        if (cached_nothing) {
            return null;
        }

        if (cache.size() == 0) {
            fillCache(seeds);
        }

        if (cache.size() == 0) {
            cached_nothing = true;
            return null;
        }

        return cache.get(0);
    }

    public void newSeed(ItemStack seed) {
        if (isMatch(seed)) {
            addToCache(seed);
        }
    }

    public void lostSeed(ItemStack seed) {
        removeFromCache(seed);
    }

    public boolean isMatch(ItemStack seed) {
        short id = ItemCropSeed.getIdFromStack(seed);
        byte scan = ItemCropSeed.getScannedFromStack(seed);

        if (scan == 0) {
            return allow_unknown_type && allow_unknown_ggr;
        }

        if (seed_type != -1 && seed_type != id) {
            return false;
        }

        if (scan < 4) {
            return allow_unknown_ggr;
        }

        byte growth = ItemCropSeed.getGrowthFromStack(seed);
        byte gain = ItemCropSeed.getGainFromStack(seed);
        byte resistance = ItemCropSeed.getResistanceFromStack(seed);

        if (growth < min_growth || growth > max_growth) {
            return false;
        }

        if (gain < min_gain || gain > max_gain) {
            return false;
        }

        if (resistance < min_resistance || resistance > max_resistance) {
            return false;
        }

        int total = growth + gain + resistance;
        if (total < min_total || total > max_total) {
            return false;
        }

        return true;
    }

    protected void fillCache(Collection<ItemStack> seeds) {
        cache.clear();

        for (ItemStack seed : seeds) {
            newSeed(seed);
        }
    }

    protected void addToCache(ItemStack seed) {
        cached_nothing = false;

        int pos = 0;
        for (int i=cache.size()-1; i>=0; i--) {
            int cmp = sort.compare(seed, cache.get(i));
            if (cmp <= 0) {
                pos = i + 1;
                break;
            }
        }

        if (pos >= CACHE_SIZE) {
            return;
        }

        cache.add(pos, seed);

        while (cache.size() > CACHE_SIZE) {
            cache.remove(cache.size() - 1);
        }

        return;
    }

    protected void removeFromCache(ItemStack seed) {
        cache.remove(seed);
    }

}
