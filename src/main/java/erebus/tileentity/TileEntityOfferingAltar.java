package erebus.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erebus.core.helper.Utils;
import erebus.network.PacketPipeline;
import erebus.network.client.PacketOfferingAltar;
import erebus.recipes.OfferingAltarRecipe;

public class TileEntityOfferingAltar extends TileEntityBasicInventory
{
	@SideOnly(Side.CLIENT)
	private EntityItem ghostItem;
	public int time = 0;
	private ItemStack output = null;

	private static final int MAX_TIME = 430;

	public TileEntityOfferingAltar()
	{
		super(4, "offeringAltar");
	}

	@SideOnly(Side.CLIENT)
	public EntityItem getItemForRendering(int slot)
	{
		if (ghostItem == null)
		{
			ghostItem = new EntityItem(worldObj);
			ghostItem.hoverStart = 0.0F;
		}

		if (inventory[slot] == null)
		{
			return null;
		} else
		{
			ghostItem.setEntityItemStack(inventory[slot]);
			return ghostItem;
		}
	}

	public void popStack()
	{
		if (!worldObj.isRemote)
		{
			for (int i = getSizeInventory() - 1; i >= 0; i--)
			{
				if (inventory[i] != null)
				{
					Utils.dropStack(worldObj, xCoord, yCoord + 1, zCoord, inventory[i].copy());
					inventory[i] = null;
					markDirty();
					return;
				}
			}
		}
	}

	public void addStack(ItemStack stack)
	{
		if (stack == null || stack.stackSize <= 0)
		{
			return;
		}
		if (inventory[3] == null)
		{
			for (int i = 0; i < 3; i++)
			{
				if (inventory[i] == null)
				{
					addStack(i, stack);
					return;
				}
			}
		}
	}

	private void addStack(int slot, ItemStack stack)
	{
		if (!worldObj.isRemote)
		{
			inventory[slot] = ItemStack.copyItemStack(stack);
			inventory[slot].stackSize = 1;
			stack.stackSize--;
			markDirty();
		}
	}

	@Override
	public void updateEntity()
	{
		if (output == null)
		{
			time = 0;
		} else
		{
			time++;
			if (!worldObj.isRemote)
			{
				if (time >= MAX_TIME)
				{
					inventory[3] = output;
					for (int i = 0; i < 3; i++)
					{
						if (inventory[i] != null)
						{
							if (--inventory[i].stackSize <= 0)
							{
								inventory[i] = null;
							}
						}
					}
					time = 0;
					markDirty();
				}
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		if (packet.func_148853_f() == 0)
		{
			readFromNBT(packet.func_148857_g());
		}
	}

	@Override
	public void markDirty()
	{
		super.markDirty();

		output = OfferingAltarRecipe.getOutput(inventory[0], inventory[1], inventory[2]);

		if (worldObj != null && !worldObj.isRemote)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			PacketPipeline.sendToAll(new PacketOfferingAltar(xCoord, yCoord, zCoord, nbt));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 2, yCoord + 3, zCoord + 2);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		time = nbt.getInteger("time");

		markDirty();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("time", time);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return slot != 3;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return slot == 3;
	}
}