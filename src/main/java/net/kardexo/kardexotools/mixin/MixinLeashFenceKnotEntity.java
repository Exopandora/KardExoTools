package net.kardexo.kardexotools.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(LeashFenceKnotEntity.class)
public abstract class MixinLeashFenceKnotEntity extends HangingEntity
{
	protected MixinLeashFenceKnotEntity(EntityType<? extends HangingEntity> entityType, Level level)
	{
		super(entityType, level);
	}
	
	@Inject
	(
		method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
		at = @At("HEAD"),
		cancellable = true
	)
	@SuppressWarnings("resource")
	public void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info)
	{
		if(!KardExo.CONFIG.getData().doPickupLeashKnots())
		{
			return;
		}
		
		if(this.level().isClientSide)
		{
			info.setReturnValue(InteractionResult.SUCCESS);
		}
		else
		{
			double radius = 7.0D;
			AABB aabb = new AABB(this.getX() - radius, this.getY() - radius, this.getZ() - radius, this.getX() + radius, this.getY() + radius, this.getZ() + radius);
			List<Mob> entities = this.level().getEntitiesOfClass(Mob.class, aabb);
			boolean playerPickup = !player.isShiftKeyDown();
			
			if(playerPickup)
			{
				boolean playerIsCarryingMobs = false;
				
				for(Mob mob : entities)
				{
					if(mob.isLeashed() && mob.getLeashHolder() == player)
					{
						mob.setLeashedTo((LeashFenceKnotEntity) (Object) this, true);
						playerIsCarryingMobs = true;
					}
				}
				
				if(!playerIsCarryingMobs)
				{
					this.discard();
					
					for(Mob mob : entities)
					{
						if(mob.isLeashed() && mob.getLeashHolder() == (LeashFenceKnotEntity) (Object) this)
						{
							mob.setLeashedTo(player, true);
						}
					}
				}
			}
			else
			{
				this.discard();
				
				if(player.getAbilities().instabuild)
				{
					for(Mob mob : entities)
					{
						if(mob.isLeashed() && mob.getLeashHolder() == (LeashFenceKnotEntity) (Object) this)
						{
							mob.dropLeash(true, false);
						}
					}
				}
			}
			
			info.setReturnValue(InteractionResult.CONSUME);
		}
	}
}
