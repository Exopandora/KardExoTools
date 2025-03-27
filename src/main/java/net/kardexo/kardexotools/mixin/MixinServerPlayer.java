package net.kardexo.kardexotools.mixin;

import com.mojang.authlib.GameProfile;
import net.kardexo.kardexotools.util.PropertyUtils;
import net.kardexo.kardexotools.util.SittingState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player
{
	@Unique
	private final SittingState sittingState = new SittingState();
	@Unique
	private static final boolean IS_SITTING_ENABLED = false;
	
	public MixinServerPlayer(Level level, BlockPos blockPos, float yRot, GameProfile gameProfile)
	{
		super(level, blockPos, yRot, gameProfile);
	}
	
	@Inject
	(
		method = "attack(Lnet/minecraft/world/entity/Entity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void attack(Entity entity, CallbackInfo info)
	{
		if(!PropertyUtils.canInteractWithEntity(this, entity))
		{
			info.cancel();
		}
	}
	
	@Inject
	(
		method = "tick",
		at = @At("TAIL")
	)
	private void tick(CallbackInfo info)
	{
		if(!IS_SITTING_ENABLED)
		{
			return;
		}
		
		if(this.sittingState.getVehicle() != null)
		{
			if(!this.sittingState.getVehicle().isAlive())
			{
				this.sittingState.setVehicle(null);
			}
			else
			{
				this.sittingState.getVehicle().setYRot(this.getYRot());
			}
		}
		
		if(!this.onGround() && this.sittingState.getVehicle() == null && this.getMainHandItem().is(Items.AIR) && this.getOffhandItem().is(Items.AIR))
		{
			return;
		}
		
		if(this.isShiftKeyDown() && !this.sittingState.isShiftKeyDownO())
		{
			ServerLevel level = (ServerLevel) this.level();
			
			if(this.sittingState.getVehicle() == null && this.sittingState.getLastShiftDownTime() > 0 && (level.getGameTime() - this.sittingState.getLastShiftDownTime()) < 10)
			{
				Vec3 position;
				BlockState state = level.getBlockState(this.blockPosition());
				
				if(state.is(BlockTags.STAIRS))
				{
					position = this.blockPosition().getCenter();
				}
				else if(state.is(Blocks.AIR) && level.getBlockState(this.blockPosition().below()).is(BlockTags.STAIRS))
				{
					position = this.blockPosition().below().getCenter();
				}
				else
				{
					position = this.position();
				}
				
				Display.BlockDisplay blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
				blockDisplay.setPortalCooldown(Integer.MAX_VALUE);
				blockDisplay.move(MoverType.SELF, position);
				level.addFreshEntityWithPassengers(blockDisplay);
				this.setShiftKeyDown(false);
				this.startRiding(blockDisplay);
				this.setShiftKeyDown(true);
				this.sittingState.setVehicle(blockDisplay);
				this.sittingState.setSitDownTime(level.getGameTime());
			}
		}
		else if(!this.isShiftKeyDown() && this.sittingState.isShiftKeyDownO())
		{
			this.sittingState.setLastShiftDownTime(this.level().getGameTime());
		}
		
		this.sittingState.setIsShiftKeyDownO(this.isShiftKeyDown());
	}
	
	@Override
	protected boolean wantsToStopRiding()
	{
		return super.wantsToStopRiding() && (!IS_SITTING_ENABLED || (this.sittingState.getVehicle() == null || this.sittingState.getLastShiftDownTime() > this.sittingState.getSitDownTime()));
	}
	
	@Override
	public void stopRiding()
	{
		if(IS_SITTING_ENABLED && this.getVehicle() != null && this.getVehicle().equals(this.sittingState.getVehicle()))
		{
			Entity vehicle = this.sittingState.getVehicle();
			this.sittingState.setVehicle(null);
			vehicle.discard();
		}
		
		super.stopRiding();
	}
	
	@Inject
	(
		method = "disconnect",
		at = @At("HEAD")
	)
	private void disconnect(CallbackInfo info)
	{
		if(IS_SITTING_ENABLED && this.sittingState.getVehicle() != null)
		{
			Entity vehicle = this.sittingState.getVehicle();
			this.sittingState.setVehicle(null);
			vehicle.discard();
		}
	}
	
	@Inject
	(
		method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
		at = @At("HEAD")
	)
	private void die(DamageSource damageSource, CallbackInfo info)
	{
		if(IS_SITTING_ENABLED && this.sittingState.getVehicle() != null)
		{
			Entity vehicle = this.sittingState.getVehicle();
			this.sittingState.setVehicle(null);
			vehicle.discard();
		}
		
		int x = Mth.floor(this.getX());
		int y = Mth.floor(this.getY());
		int z = Mth.floor(this.getZ());
		this.displayClientMessage(Component.literal("You died at " + x + " " + y + " " + z), false);
	}
}
