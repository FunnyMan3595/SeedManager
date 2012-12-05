package org.ldg.seedmanager;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.TileEntity;

public class SeedManagerBlockRenderer implements ISimpleBlockRenderingHandler {
    public int renderID;

    public SeedManagerBlockRenderer(int renderID) {
        this.renderID = renderID;
    }

//public interface ISimpleBlockRenderingHandler {
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks render) {
        if(block instanceof SeedManagerBlock) {
            ((SeedManagerBlock)block).setInventoryRender(true);
        }

        int oldValue = render.uvRotateTop;
        render.uvRotateTop = 0;
        render.renderBlockAsItem(block, metadata, 1.0F);
        render.uvRotateTop = oldValue;

        if(block instanceof SeedManagerBlock) {
            ((SeedManagerBlock)block).setInventoryRender(false);
        }
    }

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks render) {
        int oldValue = render.uvRotateTop;

        TileEntity te = world.getBlockTileEntity(x, y, z);
        if(te instanceof IHasFront) {
            int front = ((IHasFront)te).getFront();
            render.uvRotateTop = (front + 1) % 4;
        }

        render.renderStandardBlock(block, x, y, z);
        render.uvRotateTop = oldValue;
        return true; // Nobody ever checks this.
    }

    public boolean shouldRender3DInInventory() {
        return true;
    }

    public int getRenderId() {
        return this.renderID;
    }
//}
}
