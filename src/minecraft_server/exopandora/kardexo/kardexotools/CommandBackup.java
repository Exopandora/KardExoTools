package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandBackup extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "backup";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/backup";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		server.addScheduledTask(Tasks.BACKUP);
	}
}
