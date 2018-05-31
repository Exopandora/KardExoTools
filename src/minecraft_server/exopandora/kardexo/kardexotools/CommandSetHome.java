package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandSetHome extends CommandBase
{
	@Override
	public String getName()
	{
		return "sethome";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/sethome";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		BlockPos pos = sender.getCommandSenderEntity().getPosition();
		Config.HOME.getData().put(sender.getName(), new Home(pos, sender.getName(), sender.getCommandSenderEntity().dimension));
		sender.sendMessage(new TextComponentString("Home set to " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}
