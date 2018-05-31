package exopandora.kardexo.kardexotools;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandVeinminer extends CommandBase
{
	@Override
	public String getName()
	{
		return "veinminer";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/veinminer <on|off>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			if(args[0].equals("on"))
			{
				Config.VEINMINER.getData().put(sender.getName(), new VeinminerOption(sender.getName(), true));
				sender.sendMessage(new TextComponentString("Veinminer enabled"));
			}
			else if(args[0].equals("off"))
			{
				Config.VEINMINER.getData().put(sender.getName(), new VeinminerOption(sender.getName(), false));
				sender.sendMessage(new TextComponentString("Veinminer disabled"));
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
		return args.length == 1 ? this.getListOfStringsMatchingLastWord(args, new String[] {"on", "off"}) : Collections.<String>emptyList();
	}
}
