package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.mixinducks.IChair;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.class)
public abstract class MixinDisplay extends Entity
{
	public MixinDisplay(EntityType<?> entityType, Level level)
	{
		super(entityType, level);
	}
	
	@Inject
	(
		method = "tick",
		at = @At("TAIL")
	)
	private void tick(CallbackInfo ci)
	{
		if(!this.level().isClientSide() && this instanceof IChair && this.getPassengers().isEmpty())
		{
			this.discard();
		}
	}
}
