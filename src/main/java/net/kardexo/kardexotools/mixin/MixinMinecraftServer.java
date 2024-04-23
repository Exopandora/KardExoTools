package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer
{
	@Inject
	(
		method = "stopServer()V",
		at = @At("TAIL")
	)
	private void stop(CallbackInfo info)
	{
		KardExo.stop();
	}
	
	@Inject
	(
		method = "getServerModName",
		at = @At("RETURN"),
		cancellable = true,
		remap = false
	)
	public void getServerModName(CallbackInfoReturnable<String> cir)
	{
		cir.setReturnValue("KardExo");
	}
}
