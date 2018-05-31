package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandMoonPhase extends CommandBase
{
	@Override
	public String getName()
	{
		return "moonphase";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/moonphase";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		String[] phases = new String[]{"Full Moon", "Waning Gibbous", "Last Quarter", "Waning Crescent", "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous"};
		sender.sendMessage(new TextComponentString(phases[server.worlds[0].provider.getMoonPhase(server.worlds[0].getWorldTime())]));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}
