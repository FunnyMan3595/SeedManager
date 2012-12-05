package org.ldg.seedmanager;

import cpw.mods.fml.common.network.IGuiHandler;
import ic2.common.ContainerElectricMachine;
import ic2.common.IHasGui;
import net.minecraft.src.TileEntity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
public class SeedManagerGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world,
                                      int x, int y, int z) {
        TileEntity seedmanager = world.getBlockTileEntity(x, y, z);

        if (seedmanager instanceof SeedLibraryTileEntity) {
            return new SeedLibraryContainer(player.inventory, (SeedLibraryTileEntity)seedmanager);
        } else if (seedmanager instanceof SeedAnalyzerTileEntity) {
            return (ContainerElectricMachine) ((IHasGui)seedmanager).getGuiContainer(player);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world,
                                      int x, int y, int z) {
        TileEntity seedmanager = world.getBlockTileEntity(x, y, z);

        if (seedmanager instanceof SeedLibraryTileEntity) {
            return new SeedLibraryGUI(player.inventory, seedmanager);
        } else if (seedmanager instanceof SeedAnalyzerTileEntity) {
            return new SeedAnalyzerGUI((ContainerElectricMachine) ((IHasGui)seedmanager).getGuiContainer(player));
        }

        return null;
    }
}
