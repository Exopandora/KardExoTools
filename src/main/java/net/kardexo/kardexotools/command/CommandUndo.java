package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.veinminer.Veinminer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandUndo
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("undo")
				.executes(context -> undo(context.getSource())));
	}
	
	private static int undo(CommandSourceStack source) throws CommandSyntaxException
	{
		if(source.getEntity() instanceof ServerPlayer && Veinminer.hasUndo(source.getTextName()))
		{
			try
			{
				int result = Veinminer.undo(source.getPlayerOrException(), source.getServer());
				
				if(result == 0)
				{
					throw CommandBase.exception("Cannot be undone");
				}
				
				return result;
			}
			catch(Exception e)
			{
				throw CommandBase.exception(e.getMessage());
			}
		}
		
		throw CommandBase.exception("Nothing to undo");
	}
}
