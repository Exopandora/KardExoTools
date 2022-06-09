package net.kardexo.kardexotools.command;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class KardExoCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("kardexo")
				.then(Commands.literal("version")
					.executes(context -> version(context.getSource())))
				.then(Commands.literal("commands")
					.executes(context -> commands(context.getSource()))));
	}
	
	private static int version(CommandSourceStack source)
	{
		source.sendSuccess(Component.literal("Version: " + KardExo.VERSION), false);
		return 1;
	}
	
	private static int commands(CommandSourceStack source) throws CommandSyntaxException
	{
		source.sendSuccess(Component.literal("Commands:"), false);
		
		CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<CommandSourceStack>();
		KardExo.registerCommands(dispatcher);
		Map<CommandNode<CommandSourceStack>, String> usage = dispatcher.getSmartUsage(dispatcher.getRoot(), source);
		
		for(String command : usage.values())
		{
			source.sendSuccess(Component.literal(" /" + command), false);
		}
		
		return usage.values().size();
	}
}
