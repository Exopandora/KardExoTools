package net.kardexo.kardexotools.mixin;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import carpet.commands.SpawnCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

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
		root.getChildren().forEach(child -> MixinSpawnCommand.setRequirement(child, root.getRequirement()));
		MixinSpawnCommand.setRequirement(root, source -> true);
	}
	
	private static final void setRequirement(CommandNode<CommandSourceStack> node, Predicate<CommandSourceStack> requirement)
	{
		try
		{
			Field field = CommandNode.class.getDeclaredField("requirement");
			field.setAccessible(true);
			field.set(node, requirement);
		}
		catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
}
