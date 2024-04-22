package net.kardexo.kardexotools.mixin;

import carpet.commands.SpawnCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.kardexo.kardexotools.KardExo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.function.Predicate;

@Mixin(SpawnCommand.class)
public class MixinSpawnCommand
{
	@Inject
	(
		method = "register",
		at = @At("TAIL"),
		remap = false
	)
	private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, CallbackInfo info)
	{
		CommandNode<CommandSourceStack> root = dispatcher.getRoot().getChild("spawn");
		root.getChildren().forEach(child -> MixinSpawnCommand.kardexotools$setRequirement(child, root.getRequirement()));
		MixinSpawnCommand.kardexotools$setRequirement(root, source -> true);
	}
	
	@Unique
	private static void kardexotools$setRequirement(CommandNode<CommandSourceStack> node, Predicate<CommandSourceStack> requirement)
	{
		try
		{
			Field field = CommandNode.class.getDeclaredField("requirement");
			field.setAccessible(true);
			field.set(node, requirement);
		}
		catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			KardExo.LOGGER.error(e);
		}
	}
}
