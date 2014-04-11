package erebus.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erebus.entity.EntityDragonfly;
import erebus.entity.EntityScorpion;

@SideOnly(Side.CLIENT)
public class MobGrabbingHealthBarRemoval extends Gui {

	private final Minecraft mc = Minecraft.getMinecraft();

	@ForgeSubscribe
	public void onRenderHUD(Pre event) {
		if (event.type.equals(RenderGameOverlayEvent.ElementType.HEALTHMOUNT)) {
			EntityClientPlayerMP player = mc.thePlayer;
			if (player != null && player.ridingEntity != null)
				if (player.ridingEntity instanceof EntityScorpion || player.ridingEntity instanceof EntityDragonfly)
					event.setCanceled(true);
		}
	}
}