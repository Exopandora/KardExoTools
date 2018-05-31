package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandUndo extends CommandBase
{
	@Override
	public String getName()
	{
		return "undo";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/undo";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(sender.getCommandSenderEntity() instanceof EntityPlayerMP && Veinminer.hasUndo(sender.getName()))
		{
			if(!Veinminer.undo((EntityPlayerMP) sender.getCommandSenderEntity(), server.getEntityWorld()))
			{
				throw new CommandException("Cannot be undone");
			}
		}
		else
		{
			throw new CommandException("Nothing to undo");
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}
