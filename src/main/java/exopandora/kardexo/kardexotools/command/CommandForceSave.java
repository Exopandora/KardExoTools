package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.tasks.Tasks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandForceSave
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("forcesave")
				.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> save(context.getSource())));
	}
	
	private static int save(CommandSource source) throws CommandSyntaxException
	{
		source.getServer().addScheduledTask(Tasks.SAVE);
		return 1;
	}
}
