package net.kardexo.kardexotools.mixin;

import com.mojang.authlib.GameProfile;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.mixinducks.IChair;
import net.kardexo.kardexotools.mixinducks.ISittingCapableEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ISittingCapableEntity
{
	@Unique
	private final SittingState sittingState = new SittingState();
	
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
		if(this.onGround() && KardExo.PLAYERS.get(this.uuid).isSittingEnabled())
		{
			Entity chair = this.getChair();
			
			if(chair != null)
			{
				chair.setYRot(this.getYRot());
			}
			
			if(this.isShiftKeyDown() && !this.sittingState.isShiftKeyDownO())
			{
				if(chair == null && this.sittingState.getLastShiftDownTime() > 0 && (this.level().getGameTime() - this.sittingState.getLastShiftDownTime()) < 10)
				{
					this.setShiftKeyDown(false);
					this.kardexotools$startSitting();
					this.sittingState.setSitDownTime(this.level().getGameTime());
				}
			}
			else if(!this.isShiftKeyDown() && this.sittingState.isShiftKeyDownO())
			{
				this.sittingState.setLastShiftDownTime(this.level().getGameTime());
			}
			
			this.sittingState.setIsShiftKeyDownO(this.isShiftKeyDown());
		}
	}
	
	@Inject
	(
		method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
		at = @At("HEAD")
	)
	private void die(DamageSource damageSource, CallbackInfo info)
	{
		int x = Mth.floor(this.getX());
		int y = Mth.floor(this.getY());
		int z = Mth.floor(this.getZ());
		this.displayClientMessage(Component.literal("You died at " + x + " " + y + " " + z), false);
	}
	
	@Unique
	private @Nullable Entity getChair()
	{
		Entity vehicle = this.getVehicle();
		
		if(vehicle instanceof IChair)
		{
			return vehicle;
		}
		
		return null;
	}
	
	@Override
	public void kardexotools$startSitting()
	{
		if(!this.level().isClientSide())
		{
			ServerLevel level = (ServerLevel) this.level();
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
			((IChair) blockDisplay).kardexotools$setChair(true);
			level.addFreshEntityWithPassengers(blockDisplay);
			this.startRiding(blockDisplay);
		}
	}
	
	@Override
	public void kardexotools$stopSitting()
	{
		this.stopRiding();
	}
	
	@Override
	public boolean kardexotools$isSitting()
	{
		return this.getChair() != null;
	}
}
