package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.kardexo.kardexotools.property.PropertyHelper;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer
{
	@Inject
	(
		method = "attack(Lnet/minecraft/world/entity/Entity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void attack(Entity entity, CallbackInfo info)
	{
		if(!PropertyHelper.canInteractWithEntity((ServerPlayer) (Object) this, entity))
		{
			info.cancel();
		}
	}
	
	@Inject
	(
		method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
		at = @At("HEAD")
	)
	private void die(DamageSource damageSource, CallbackInfo info)
	{
		ServerPlayer player = (ServerPlayer) (Object) this;
		
		int x = Mth.floor(player.getX());
		int y = Mth.floor(player.getY());
		int z = Mth.floor(player.getZ());
		
		player.sendMessage(new TextComponent("You died at " + x + " " + y + " " + z), Util.NIL_UUID);
	}
}
