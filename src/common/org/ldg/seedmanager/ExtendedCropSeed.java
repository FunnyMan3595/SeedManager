package org.ldg.seedmanager;

import ic2.common.ItemCropSeed;
import ic2.common.Ic2Items;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemStack;
import java.util.List;

public class ExtendedCropSeed extends ItemCropSeed {
    public static final String yellow = '\247' + "e";
    public static final String green = '\247' + "a";
    public static final String black = '\247' + "0";

    public ExtendedCropSeed(ItemStack parent) {
        super(parent.itemID-256, 152);
        //setItemName("itemCropSeed");
        this.setCreativeTab(CreativeTabs.tabMaterials);
    }

    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, List list) {
        super.addInformation(stack, list);
        if (getScannedFromStack(stack) == 4) {
            int growth = getGrowthFromStack(stack);
            int gain = getGainFromStack(stack);
            int resistance = getResistanceFromStack(stack);
            int total = growth + gain + resistance;
            list.add(yellow + "Lv " + total);
        }
    }

/*    @SuppressWarnings("unchecked")
    public void getSubItems(int id, CreativeTabs category, List tabContents) {
    }*/
}
