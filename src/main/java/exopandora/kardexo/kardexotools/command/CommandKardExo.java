package exopandora.kardexo.kardexotools.command;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import exopandora.kardexo.kardexotools.KardExo;
import exopandora.kardexo.kardexotools.data.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandKardExo
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("kardexo")
				.then(Commands.literal("version")
					.executes(context -> version(context.getSource())))
				.then(Commands.literal("commands")
					.executes(context -> commands(context.getSource()))));
	}
	
	private static int version(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("Version: " + Config.VERSION), false);
		return 1;
	}
	
	private static int commands(CommandSource source) throws CommandSyntaxException
	{
		source.sendFeedback(new StringTextComponent("Commands:"), false);
		
		CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
		KardExo.registerCommands(dispatcher);
		Map<CommandNode<CommandSource>, String> usage = dispatcher.getSmartUsage(dispatcher.getRoot(), source);
		
		for(String command : usage.values())
		{
			source.sendFeedback(new StringTextComponent(" /" + command), false);
		}
		
		return usage.values().size();
	}
}
