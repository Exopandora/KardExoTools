package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandForceSave extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "forcesave";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/forcesave";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		server.addScheduledTask(Tasks.SAVE);
	}
}
