package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeashFenceKnotEntity.class)
public abstract class MixinLeashFenceKnotEntity extends BlockAttachedEntity
{
	protected MixinLeashFenceKnotEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level)
	{
		super(entityType, level);
	}
	
	@Inject
	(
		method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
		at = @At("HEAD"),
		cancellable = true
	)
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
			List<Leashable> entities = LeadItem.leashableInArea(this.level(), this.getPos(), leashable ->
			{
				Entity entity = leashable.getLeashHolder();
				return entity == player || entity == this;
			});
			boolean playerPickup = !player.isShiftKeyDown();
			
			if(playerPickup)
			{
				boolean playerIsCarryingMobs = false;
				
				for(Leashable leashable : entities)
				{
					if(leashable.isLeashed() && leashable.getLeashHolder() == player)
					{
						leashable.setLeashedTo(this, true);
						playerIsCarryingMobs = true;
					}
				}
				
				if(!playerIsCarryingMobs)
				{
					this.discard();
					
					for(Leashable leashable : entities)
					{
						if(leashable.isLeashed() && leashable.getLeashHolder() == this)
						{
							leashable.setLeashedTo(player, true);
						}
					}
				}
			}
			else
			{
				this.discard();
				
				if(player.getAbilities().instabuild)
				{
					for(Leashable leashable : entities)
					{
						if(leashable.isLeashed() && leashable.getLeashHolder() == this)
						{
							leashable.dropLeash(true, false);
						}
					}
				}
			}
			
			info.setReturnValue(InteractionResult.CONSUME);
		}
	}
}
