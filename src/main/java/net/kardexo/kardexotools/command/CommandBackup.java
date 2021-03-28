package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.tasks.TaskBackup;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandBackup
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("backup")
				.requires(source -> source.hasPermission(2))
					.executes(context -> backup(context.getSource())));
	}
	
	private static int backup(CommandSource source) throws CommandSyntaxException
	{
		new TaskBackup(source.getServer()).execute();
		return 1;
	}
}
