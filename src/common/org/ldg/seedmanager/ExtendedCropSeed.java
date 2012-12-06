package org.ldg.seedmanager;

import ic2.api.CropCard;
import ic2.common.ItemCropSeed;
import ic2.common.Ic2Items;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
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

    @Override
    @SuppressWarnings("unchecked")
    public void getSubItems(int id, CreativeTabs tabs, List items) {
        super.getSubItems(id, tabs, items);

        for (int scan=0; scan<5; scan++) {
            items.add(generateItemStackFromValues((short)5, (byte)1, (byte)1, (byte)1, (byte)scan));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flag) {
        if (getScannedFromStack(stack) == -42) {
            // This is a custom setting for the fake display seed used by SeedAnalyzerTileEntityUU
            list.clear();

            String name = "Unknown Seeds";
            int id = getIdFromStack(stack);
            if (id != -1) {
                name = CropCard.getCrop(id).name();
            }
            list.add(name);
            list.add("\u00a72Gr\u00a77 ?");
            list.add("\u00a76Ga\u00a77 ?");
            list.add("\u00a73Re\u00a77 ?");
            list.add(yellow + "Lv ?");

            if (player.capabilities.isCreativeMode) {
                list.add("\u00a7oSc " + getScannedFromStack(stack));
            }

            return;
        }

        super.addInformation(stack, player, list, flag);
        if (getScannedFromStack(stack) == 4) {
            int growth = getGrowthFromStack(stack);
            int gain = getGainFromStack(stack);
            int resistance = getResistanceFromStack(stack);
            int total = growth + gain + resistance;
            list.add(yellow + "Lv " + total);
        }

        if (player.capabilities.isCreativeMode) {
            list.add("\u00a7oSc " + getScannedFromStack(stack));
        }
    }

/*    @SuppressWarnings("unchecked")
    public void getSubItems(int id, CreativeTabs category, List tabContents) {
    }*/
}
