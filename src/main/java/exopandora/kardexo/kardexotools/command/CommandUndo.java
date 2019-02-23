package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.veinminer.Veinminer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandUndo
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("undo")
				.executes(context -> undo(context.getSource())));
	}
	
	private static int undo(CommandSource source) throws CommandSyntaxException
	{
		if(source.getEntity() instanceof EntityPlayerMP && Veinminer.hasUndo(source.getName()))
		{
			try
			{
				int result = Veinminer.undo(source.asPlayer(), source.getServer());
				
				if(result == 0)
				{
					throw CommandBase.createException("Cannot be undone");
				}
				
				return result;
			}
			catch(Exception e)
			{
				throw CommandBase.createException(e.getMessage());
			}
		}
		
		throw CommandBase.createException("Nothing to undo");
	}
}
