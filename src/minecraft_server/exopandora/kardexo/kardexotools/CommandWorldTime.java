package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;

public class CommandWorldTime extends CommandBase
{

	@Override
	public String getName()
	{
		return "worldtime";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/worldtime";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		sender.sendMessage(new TextComponentString("World time: " + this.getWorldTime(server.getEntityWorld().getWorldTime())));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
		return true;
    }
	
	public int getHour(long tick)
	{
		int hour = MathHelper.floor((tick + 6000) / 1000F) % 24;
		
		return hour;
	}
	
	public int getMinute(long tick)
	{
		int hour = MathHelper.floor((tick + 6000F) / 1000F);
		int minute = MathHelper.floor((tick + 6000F - hour * 1000) * 6 / 100);
		
		return minute;
	}
	
	public String getWorldTime(long tick)
	{
		int hour = this.getHour(tick);
		int minute = this.getMinute(tick);
		
		return String.format("%02d:%02d", hour, minute);
	}
}
