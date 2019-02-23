package exopandora.kardexo.kardexotools.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import exopandora.kardexo.kardexotools.KardExo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

public class TickableSleep implements ITickable
{
	private final MinecraftServer server;
	private final Map<String, Long> sleep = new HashMap<String, Long>();
	
	public TickableSleep(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void tick()
	{
		WorldServer overworld = this.server.getWorld(DimensionType.OVERWORLD);
		
		if(!overworld.playerEntities.isEmpty())
		{
			for(EntityPlayer player : overworld.playerEntities)
			{
				String playername = player.getName().getString();
				
				if(player.isPlayerSleeping())
				{
					if(!this.sleep.containsKey(playername) && !overworld.isDaytime())
					{
						KardExo.notifyPlayers(this.server, new TextComponentTranslation("%s is now sleeping", player.getDisplayName()));
						this.sleep.put(playername, overworld.getDayTime());
					}
					
					if((this.sleep.get(playername) + 100) <= overworld.getWorldInfo().getDayTime())
					{
						for(WorldServer server : this.server.getWorlds())
						{
							long i = server.getWorldInfo().getDayTime() + 24000L;
							server.getWorldInfo().setDayTime(i - i % 24000L);
							
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
						if(!overworld.isDaytime())
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
