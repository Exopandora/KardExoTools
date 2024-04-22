package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.tasks.BackupTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerList
{
	@Inject
	(
		method = "remove(Lnet/minecraft/server/level/ServerPlayer;)V",
		at = @At("HEAD")
	)
	private void remove(ServerPlayer player, CallbackInfo info)
	{
		BackupTask.onPlayerLoggedOut();
	}
}
