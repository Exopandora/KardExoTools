package exopandora.kardexo.kardexotools;

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
	private List<String> cache = new ArrayList<String>();
	
	public TickableDeathListener(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void update()
	{
		for(EntityPlayerMP player : this.server.getPlayerList().getPlayerList())
		{
			if(player.getHealth() == 0)
			{
				if(!this.cache.contains(player.getName()))
				{
					player.addChatMessage(new TextComponentString("You died at " + MathHelper.floor(player.posX) + " " + MathHelper.floor(player.posY) + " " + MathHelper.floor(player.posZ)));
					this.cache.add(player.getName());
				}
			}
			else
			{
				if(this.cache.contains(player.getName()))
				{
					this.cache.remove(player.getName());
				}
			}
		}
	}
}
