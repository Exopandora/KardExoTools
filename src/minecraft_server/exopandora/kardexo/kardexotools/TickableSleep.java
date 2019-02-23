package exopandora.kardexo.kardexotools;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

public class TickableSleep implements ITickable
{
	private final MinecraftServer server;
	private final Map<String, Long> sleep = new HashMap<String, Long>();
	
	public TickableSleep(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void update()
	{
		if(!this.server.getEntityWorld().playerEntities.isEmpty())
		{
			for(EntityPlayer player : this.server.getPlayerList().getPlayers())
			{
				String playername = player.getName();
				
				if(player.isPlayerSleeping())
				{
					if(!this.sleep.containsKey(playername) && !this.server.getEntityWorld().isDaytime())
					{
						KardExo.notifyPlayers(this.server, new TextComponentTranslation("%s is now sleeping", player.getDisplayName()));
						this.sleep.put(playername, this.server.getEntityWorld().getWorldInfo().getWorldTime());
					}
					
					if((this.sleep.get(playername) + 100) <= this.server.getEntityWorld().getWorldInfo().getWorldTime())
					{
						for(WorldServer server : this.server.worlds)
						{
							long i = server.getWorldInfo().getWorldTime() + 24000L;
							server.getWorldInfo().setWorldTime(i - i % 24000L);
							
							for(EntityPlayer entityplayer : server.playerEntities.stream().filter(EntityPlayer::isPlayerSleeping).collect(Collectors.toList()))
							{
								entityplayer.wakeUpPlayer(false, false, true);
							}
						}
						
						this.sleep.clear();
					}
				}
				else
				{
					if(this.sleep.containsKey(playername))
					{
						if(!this.server.getServer().getEntityWorld().isDaytime())
						{
							KardExo.notifyPlayers(this.server, new TextComponentTranslation("%s is no longer sleeping", player.getDisplayName()));
						}
						
						this.sleep.remove(playername);
					}
				}
			}
		}
	}
}
