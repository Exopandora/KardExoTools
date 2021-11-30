package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.property.PropertyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer
{
	@Inject
	(
		method = "initServer()Z",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/server/dedicated/DedicatedServer;loadLevel()V"
		)
	)
	private void init(CallbackInfoReturnable<Boolean> info)
	{
		KardExo.init((DedicatedServer) (Object) this);
	}
	
	@Inject
	(
		method = "isUnderSpawnProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player, CallbackInfoReturnable<Boolean> info)
	{
		if(!PropertyHelper.canHarvestBlock(player, blockPos))
		{
			player.getServer().getPlayerList().sendAllPlayerInfo((ServerPlayer) player);
			info.setReturnValue(true);
		}
	}
}
