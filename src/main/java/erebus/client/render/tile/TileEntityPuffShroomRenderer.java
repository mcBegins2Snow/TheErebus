package erebus.client.render.tile;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erebus.client.model.block.ModelPuffShroom;
import erebus.tileentity.TileEntityPuffShroom;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class TileEntityPuffShroomRenderer extends TileEntitySpecialRenderer {
	private static final ResourceLocation TEXTURE = new ResourceLocation("erebus:textures/special/tiles/puffShroom.png");

	private final ModelPuffShroom model = new ModelPuffShroom();

	@Override
	public final void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTickTime) {
		TileEntityPuffShroom trap = (TileEntityPuffShroom) tile;
		int meta = tile.getBlockMetadata();

		bindTexture(TEXTURE);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScaled(-1, -1, 1);
		switch (meta) {
			case 2:
				GL11.glRotatef(0F, 0.0F, 1F, 0F);
				break;
			case 3:
				GL11.glRotatef(180F, 0.0F, 1F, 0F);
				break;
			case 4:
				GL11.glRotatef(-90F, 0.0F, 1F, 0F);
				break;
			case 5:
				GL11.glRotatef(90F, 0.0F, 1F, 0F);
				break;
		}

		model.render(trap, partialTickTime);
		GL11.glPopMatrix();
	}
}