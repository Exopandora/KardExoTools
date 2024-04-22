package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
	
	@Overwrite(remap = false)
	public String getServerModName()
	{
		return "KardExo";
	}
}
