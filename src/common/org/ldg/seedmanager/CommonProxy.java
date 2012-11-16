package org.ldg.seedmanager;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;

public class CommonProxy {
    public void init(SeedManagerBlock seedmanager, ItemStack seedAnalyzer,
                     ItemStack seedLibrary) {}

    public EntityPlayer getLocalPlayer() {
        return null;
    }
}
