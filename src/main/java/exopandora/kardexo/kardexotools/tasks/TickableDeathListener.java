package exopandora.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
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
		for(ServerPlayerEntity player : this.server.func_184103_al().getPlayers())
		{
			if(player.getHealth() == 0)
			{
				if(!this.cache.contains(player.getName().getString()))
				{
					player.sendMessage(new StringTextComponent("You died at " + MathHelper.floor(player.posX) + " " + MathHelper.floor(player.posY) + " " + MathHelper.floor(player.posZ)));
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
