package exopandora.kardexo.kardexotools;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandKardExo extends CommandBase
{
	@Override
	public String getName()
	{
		return "kardexo";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/kardexo <list|commands>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			if(args[0].equals("commands"))
			{
				sender.sendMessage(new TextComponentString("Commands:"));
				
				for(ICommand command : KardExo.getCommands())
				{
					if(!command.getName().equals(this.getName()) && command.checkPermission(server, sender))
					{
						sender.sendMessage(new TextComponentString(" " + command.getUsage(sender)));
					}
				}
			}
			else if(args[0].equals("version"))
			{
				sender.sendMessage(new TextComponentString("Version: " + Config.VERSION));
			}
			else
			{
				throw new WrongUsageException(this.getUsage(sender));
			}
		}
		else
		{
			throw new WrongUsageException(this.getUsage(sender));
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? this.getListOfStringsMatchingLastWord(args, new String[] {"commands", "version"}) : Collections.<String>emptyList();
	}
}
