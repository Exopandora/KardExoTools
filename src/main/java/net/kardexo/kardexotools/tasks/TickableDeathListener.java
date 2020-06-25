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
					player.sendMessage(new StringTextComponent("You died at " + MathHelper.floor(player.getPosX()) + " " + MathHelper.floor(player.getPosY()) + " " + MathHelper.floor(player.getPosZ())), Util.field_240973_b_);
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
