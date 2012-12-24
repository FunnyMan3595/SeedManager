package org.ldg.seedmanager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CommonProxy {
    public void init(SeedManagerBlock seedmanager, ItemStack seedAnalyzer,
                     ItemStack seedLibrary) {}

    public EntityPlayer getLocalPlayer() {
        return null;
    }

    public int getRenderId() {
        return 0;
    }
}
