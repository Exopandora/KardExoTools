package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandMoonPhase extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "moonphase";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/moonphase";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		System.out.println(server.worldServers[0].provider.getMoonPhase(server.worldServers[0].getWorldTime()));
		String[] phases = new String[]{"Full Moon", "Waning Gibbous", "Last Quarter", "Waning Crescent", "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous"};
		sender.addChatMessage(new TextComponentString(phases[server.worldServers[0].provider.getMoonPhase(server.worldServers[0].getWorldTime())]));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
		return true;
    }
}
