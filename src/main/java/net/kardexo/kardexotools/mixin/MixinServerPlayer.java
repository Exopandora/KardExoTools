package net.kardexo.kardexotools.mixin;

import com.mojang.authlib.GameProfile;
import net.kardexo.kardexotools.util.PropertyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player
{
	public MixinServerPlayer(Level level, GameProfile gameProfile)
	{
		super(level, gameProfile);
	}
	
	@Inject
	(
		method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
		at = @At("HEAD")
	)
	private void die(DamageSource damageSource, CallbackInfo ci)
	{
		int x = Mth.floor(this.getX());
		int y = Mth.floor(this.getY());
		int z = Mth.floor(this.getZ());
		this.sendSystemMessage(Component.literal("You died at " + x + " " + y + " " + z));
	}
	
	@Inject
	(
		method = "sendSpawnProtectionMessage",
		at = @At("HEAD"),
		cancellable = true
	)
	private void sendSpawnProtectionMessage(BlockPos pos, CallbackInfo ci)
	{
		if(!PropertyUtils.canHarvestBlock(this, pos))
		{
			ci.cancel();
		}
	}
}
