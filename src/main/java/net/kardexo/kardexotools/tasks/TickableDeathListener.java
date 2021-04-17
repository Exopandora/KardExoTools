package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

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
		for(ServerPlayerEntity player : this.server.getPlayerList().getPlayers())
		{
			if(player.getHealth() == 0)
			{
				if(!this.cache.contains(player.getGameProfile().getName()))
				{
					int x = MathHelper.floor(player.getX());
					int y = MathHelper.floor(player.getY());
					int z = MathHelper.floor(player.getZ());
					
					player.sendMessage(new StringTextComponent("You died at " + x + " " + y + " " + z), Util.NIL_UUID);
					
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
