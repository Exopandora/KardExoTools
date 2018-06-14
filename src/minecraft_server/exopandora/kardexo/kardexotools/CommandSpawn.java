package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandSpawn extends CommandBase
{
	@Override
	public String getName()
	{
		return "spawn";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/spawn";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(sender.getCommandSenderEntity() instanceof EntityPlayer && server.getEntityWorld() != null)
		{
			CommandHome.doTeleport(server, sender.getCommandSenderEntity(), server.getEntityWorld().getTopSolidOrLiquidBlock(server.worlds[0].getSpawnPoint()), 0);
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}
