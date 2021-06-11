package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class TickableDeathListener implements Runnable
{
	private final MinecraftServer server;
	private final List<String> cache = new ArrayList<String>();
	
	public TickableDeathListener(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void run()
	{
		for(ServerPlayer player : this.server.getPlayerList().getPlayers())
		{
			if(player.getHealth() == 0)
			{
				if(!this.cache.contains(player.getGameProfile().getName()))
				{
					int x = Mth.floor(player.getX());
					int y = Mth.floor(player.getY());
					int z = Mth.floor(player.getZ());
					
					player.sendMessage(new TextComponent("You died at " + x + " " + y + " " + z), Util.NIL_UUID);
					
					this.cache.add(player.getGameProfile().getName());
				}
			}
			else
			{
				if(this.cache.contains(player.getGameProfile().getName()))
				{
					this.cache.remove(player.getGameProfile().getName());
				}
			}
		}
	}
}
