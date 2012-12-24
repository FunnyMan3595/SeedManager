package org.ldg.seedmanager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CommonProxy {
    public ItemStack seedAnalyzer;
    public ItemStack seedLibrary;

    public void init(SeedManagerBlock seedmanager, ItemStack seedAnalyzer,
                     ItemStack seedLibrary) {
        this.seedAnalyzer = seedAnalyzer;
        this.seedLibrary  = seedLibrary;
    }

    public EntityPlayer getLocalPlayer() {
        return null;
    }

    public int getRenderId() {
        return 0;
    }
}
