package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.util.PropertyUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class MixinPlayer
{
	@Inject
	(
		method = "attack(Lnet/minecraft/world/entity/Entity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void attack(Entity entity, CallbackInfo ci)
	{
		if(!PropertyUtils.canInteractWithEntity((Player) (Object) this, entity))
		{
			ci.cancel();
		}
	}
}
