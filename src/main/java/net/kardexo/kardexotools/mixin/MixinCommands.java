package net.kardexo.kardexotools.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.kardexo.kardexotools.KardExo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class MixinCommands
{
	@Final
	@Shadow
	private CommandDispatcher<CommandSourceStack> dispatcher;
	
	@Inject
	(
		method = "<init>",
		at = @At("TAIL")
	)
	private void registerCommands(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext, CallbackInfo ci)
	{
		KardExo.registerCommands(this.dispatcher, commandBuildContext);
	}
}
