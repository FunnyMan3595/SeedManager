package org.ldg.seedmanager;

import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemBlock;

public class SeedManagerItem extends ItemBlock {
    public SeedManagerItem(int i) {
        super(i);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        if (damage == SeedManagerBlock.DAMAGE_LIBRARY_LEGACY) {
            return SeedManagerBlock.DATA_LIBRARY_OFF;
        } else if (damage == SeedManagerBlock.DAMAGE_ANALYZER_LEGACY) {
            return SeedManagerBlock.DATA_ANALYZER;
        } else if (damage < SeedManagerBlock.DATA_ANALYZER) {
            return SeedManagerBlock.DATA_LIBRARY_OFF;
        } else {
            return SeedManagerBlock.DATA_ANALYZER;
        }
    }

    @Override
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
