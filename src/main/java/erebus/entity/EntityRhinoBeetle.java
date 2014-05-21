package erebus.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import erebus.ModItems;
import erebus.item.ItemErebusSpecial;
import erebus.item.ItemErebusMaterial.DATA;

public class EntityRhinoBeetle extends EntityTameable {
	private final EntityAINearestAttackableTarget aiNearestAttackableTarget = new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true);
	private boolean ramming;
	public int rammingCharge;

	public EntityRhinoBeetle(World world) {
		super(world);
		setSize(2.3F, 1.4F);
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIAttackOnCollide(this, 0.7D, true));
		tasks.addTask(2, new EntityAIMate(this, 0.5D));
		tasks.addTask(3, new EntityAITempt(this, 0.5D, ModItems.turnip.itemID, false));
		tasks.addTask(5, new EntityAIWander(this, 0.5D));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		tasks.addTask(7, new EntityAILookIdle(this));
		targetTasks.addTask(0, new EntityAIHurtByTarget(this, false));
		targetTasks.addTask(1, aiNearestAttackableTarget);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(30, new Byte((byte) 0));
		dataWatcher.addObject(31, new Byte((byte) 0));
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.7D);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(60.0D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(8.0D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setAttribute(0.75D);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	/*
	 * Disabled until sounds are found
	 * 
	 * @Override protected String getLivingSound() { return
	 * "erebus:rhinobeetlesound"; }
	 * 
	 * @Override protected String getHurtSound() { return
	 * "erebus:rhinobeetlehurt"; }
	 */
	
	@Override
	protected String getDeathSound() {
		return "erebus:squish";
	}

	@Override
	protected void playStepSound(int x, int y, int z, int blockID) {
		playSound("mob.spider.step", 0.15F, 1.0F);
	}

	@Override
	protected int getDropItemId() {
		return Item.ingotIron.itemID;
	}

	@Override
	protected void dropRareDrop(int par1) {
		dropItem(Item.ghastTear.itemID, 1);
	}

	@Override
	public boolean isOnLadder() {
		return riddenByEntity != null && isCollidedHorizontally;
	}
	
	@Override
	public boolean getCanSpawnHere() {
		float light = this.getBrightness(1.0F);
		if (light >= 0F) {
			return worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && !worldObj.isAnyLiquid(this.boundingBox);
	    }
	    return super.getCanSpawnHere();
	}
	
	@Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

	@Override
	protected boolean canDespawn() {
		if (getTameState() != 0)
			return false;
		else
			return true;
	}
	
	@Override
    public boolean allowLeashing() {
        return !canDespawn() && super.allowLeashing();
    }

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting) {
		if (getTameState() == 2)
			entityDropItem(new ItemStack(ModItems.erebusSpecialItem, 1, ItemErebusSpecial.dataRhinoRidingKit), 0.0F);
		int var3 = 1 + rand.nextInt(3) + rand.nextInt(1 + looting);
		for (int a = 0; a < var3; ++a)
			entityDropItem(new ItemStack(ModItems.erebusMaterials, 1, DATA.plateExo.ordinal()), 0.0F);
	}

	@Override
	public boolean interact(EntityPlayer player) {
		ItemStack is = player.inventory.getCurrentItem();
		float healingBuff = 0.0F;
		if (is != null && is.itemID == ModItems.erebusSpecialItem.itemID && is.getItemDamage() == 1 && getTameState() == 0) {
			healingBuff = 20F;
			is.stackSize--;
			setTameState((byte) 1);
			playTameEffect(true);
			player.swingItem();
			tasks.removeTask(aiNearestAttackableTarget);
			setAttackTarget((EntityLivingBase) null);
			getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(80.0D);
			heal(healingBuff);
			return true;
		}
		if (is != null && is.itemID == ModItems.erebusSpecialItem.itemID && is.getItemDamage() == 0 && getTameState() == 1) {
			is.stackSize--;
			player.swingItem();
			setTameState((byte) 2);
			return true;
		}
		if (is != null && is.itemID == ModItems.turnip.itemID && !isInLove() && getTameState() != 0) {
			is.stackSize--;
			inLove = 600;
			return true;
		}
		if (is == null && getTameState() == 2) {
			if (!worldObj.isRemote)
				player.mountEntity(this);
			return true;
		}
        if (is != null && is.itemID == ModItems.erebusMaterials.itemID && is.getItemDamage() == 11 && getTameState() != 0) {
        	healingBuff = 5.0F;
        	if (getHealth() < getMaxHealth()) {
        		heal(healingBuff);
        		playTameEffect(true);
    			player.swingItem();
    			is.stackSize--;
    			if (getHealth() == getMaxHealth())
    				worldObj.playSoundEffect(posX, posY, posZ, "erebus:beetlelarvamunch", 1.0F, 0.75F);
        	}
        	return true;
		}
		else
			return super.interact(player);
	}

	public void setRamAttack(boolean state) {
		ramming = state;
	}

	@Override
	protected void collideWithEntity(Entity entity) {
		if (riddenByEntity != null && entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && !(entity instanceof EntityBotFlyLarva)&& ramming){
			ram(entity, getRammingCharge() * 0.2F, getRammingCharge() * 0.4F);
			setRammingCharge((byte) 0);
		}
	super.collideWithEntity(entity);
	}
	
	@Override
    public void setAttackTarget(EntityLivingBase entity) {
		if (getTameState() != 0) {
			if (entity instanceof EntityPlayer)
				super.setAttackTarget((EntityLivingBase) null);
		}
		else
			super.setAttackTarget(entity);
    }

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (getTameState() != 0)
			if (entity instanceof EntityPlayer) {
				setAttackTarget((EntityLivingBase) null);
				return false;
			}
		return ram(entity, 1F, 6F);
	}

	private boolean ram(Entity entity, float knockback, float damage) {
		if(getTameState() == 0 || riddenByEntity == null)
			setRammingCharge((byte) 32);
		entity.attackEntityFrom(DamageSource.causeMobDamage(this), (int) damage);
		entity.addVelocity(-MathHelper.sin(rotationYaw * 3.141593F / 180.0F) * knockback, 0.4D, MathHelper.cos(rotationYaw * 3.141593F / 180.0F) * knockback);
		worldObj.playSoundAtEntity(entity, "damage.fallbig", 1.0F, 1.0F);
		((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, worldObj.difficultySetting * 50, 0));
		setRamAttack(false);
		return true;
	}
	
	@Override
	public void onUpdate() {
		if(!ramming)
			if(getTameState() == 0|| riddenByEntity == null)
				if(worldObj.getWorldTime() % 10 == 0)
					setRammingCharge((byte) 0);
		super.onUpdate();	
	}

	@Override
	public void moveEntityWithHeading(float strafe, float forward) {
		if (riddenByEntity != null) {
			prevRotationYaw = rotationYaw = riddenByEntity.rotationYaw;
			rotationPitch = riddenByEntity.rotationPitch * 0.5F;
			setRotation(rotationYaw, rotationPitch);
			rotationYawHead = renderYawOffset = rotationYaw;
			strafe = ((EntityLivingBase) riddenByEntity).moveStrafing * 0.5F;
			forward = ((EntityLivingBase) riddenByEntity).moveForward;

			if (forward <= 0.0F)
				forward *= 0.25F;

			stepHeight = 1.0F;
			jumpMovementFactor = getAIMoveSpeed() * 0.1F;

			if (!worldObj.isRemote) {
				setAIMoveSpeed((float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
				super.moveEntityWithHeading(strafe, forward);
			}
			prevLimbSwingAmount = limbSwingAmount;
			double d0 = posX - prevPosX;
			double d1 = posZ - prevPosZ;
			float f4 = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;
			travelSpeed(f4);
			if (f4 > 1.0F)
				f4 = 1.0F;

			limbSwingAmount += (f4 - limbSwingAmount) * 0.4F;
			limbSwing += limbSwingAmount;

		} else {
			stepHeight = 0.5F;
			jumpMovementFactor = 0.02F;
			super.moveEntityWithHeading(strafe, forward);
		}
	}

	private void travelSpeed(float velocity) {
		if(!worldObj.isRemote) {
		if (velocity >= 4F)
			setRammingCharge((byte) (getRammingCharge()+1));
		else if (velocity <= 4F)
			setRammingCharge((byte) (getRammingCharge()-1));
		if (getRammingCharge() <= 0)
			setRammingCharge((byte) (0));
		if (getRammingCharge() >= 25)
			setRammingCharge((byte) (25));
		}
	}
	
	public void setRammingCharge(byte velocity) {
		dataWatcher.updateObject(30, Byte.valueOf(velocity));
	}

	public byte getRammingCharge() {
		return dataWatcher.getWatchableObjectByte(30);
	}
	
	@Override
	public void updateRiderPosition() {
		super.updateRiderPosition();
		if (riddenByEntity instanceof EntityLivingBase) {
			double a = Math.toRadians(renderYawOffset);
			double offSetX = -Math.sin(a) * 0.35D;
			double offSetZ = Math.cos(a) * 0.35D;
			riddenByEntity.setPosition(posX - offSetX, posY + 1.3D + riddenByEntity.getYOffset(), posZ - offSetZ);
		}
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityageable) {
		return spawnBabyAnimal(entityageable);
	}

	@Override
	public boolean isBreedingItem(ItemStack is) {
		if (getTameState() != 0)
			return is != null && is.itemID == ModItems.turnip.itemID;
		else
			return false;
	}

	public EntityBeetleLarva spawnBabyAnimal(EntityAgeable entityageable) {
		EntityBeetleLarva entityBeetleLarva = new EntityBeetleLarva(worldObj);
		entityBeetleLarva.setTame((byte) 2);
		return entityBeetleLarva;
	}

	public void setTameState(byte tameState) {
		dataWatcher.updateObject(31, Byte.valueOf(tameState));
	}

	public byte getTameState() {
		return dataWatcher.getWatchableObjectByte(31);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setByte("tameState", getTameState());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		setTameState(nbt.getByte("tameState"));
	}
}