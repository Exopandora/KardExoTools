package exopandora.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;

public class TickableDeathListener implements ITickable
{
	private final MinecraftServer server;
	private final List<String> cache = new ArrayList<String>();
	
	public TickableDeathListener(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void tick()
	{
		for(EntityPlayerMP player : this.server.getPlayerList().getPlayers())
		{
			if(player.getHealth() == 0)
			{
				if(!this.cache.contains(player.getName().getString()))
				{
					player.sendMessage(new TextComponentString("You died at " + MathHelper.floor(player.posX) + " " + MathHelper.floor(player.posY) + " " + MathHelper.floor(player.posZ)));
					this.cache.add(player.getName().getString());
				}
			}
			else
			{
				if(this.cache.contains(player.getName().getString()))
				{
					this.cache.remove(player.getName().getString());
				}
			}
		}
	}
}
