package org.ldg.seedmanager;

import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy {
    public void init(SeedManagerBlock seedmanager, ItemStack seedAnalyzer,
                     ItemStack seedLibrary) {
        // Preload the in-world texture.
        MinecraftForgeClient.preloadTexture("/fm_seedmanager.png");

        // Set up the seed analyzer animation.
        TextureFXManager.instance().addAnimation(new SeedAnalyzerFX());

        // Add naming.
        LanguageRegistry.addName(seedmanager, "Seed Manager");
        LanguageRegistry.addName(seedAnalyzer, "Seed Analyzer");
        LanguageRegistry.addName(seedLibrary, "Seed Library");
    }

    public EntityPlayer getLocalPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }
}
