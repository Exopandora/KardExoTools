package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.server.MinecraftServer;

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
	
	/**
	 * Returns the server brand
	 * 
	 * @author Exopandora
	 * @reason Override the vanilla branding
	 * @return "KardExo"
	 */
	@Overwrite(remap = false)
	public String getServerModName()
	{
		return "KardExo";
	}
}
