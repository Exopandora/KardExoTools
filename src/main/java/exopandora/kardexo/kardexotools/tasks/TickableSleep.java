package exopandora.kardexo.kardexotools.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import exopandora.kardexo.kardexotools.KardExo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class TickableSleep implements Runnable
{
	private final MinecraftServer server;
	private final Map<String, Long> sleep = new HashMap<String, Long>();
	
	public TickableSleep(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void run()
	{
		ServerWorld overworld = this.server.getWorld(DimensionType.OVERWORLD);
		
		if(!overworld.getPlayers().isEmpty())
		{
			for(PlayerEntity player : overworld.getPlayers())
			{
				String playername = player.getName().getString();
				
				if(player.isSleeping())
				{
					if(!this.sleep.containsKey(playername) && !overworld.isDaytime())
					{
						KardExo.notifyPlayers(this.server, new TranslationTextComponent("%s is now sleeping", player.getDisplayName()));
						this.sleep.put(playername, overworld.getDayTime());
					}
					
					if((this.sleep.get(playername) + 100) <= overworld.getWorldInfo().getDayTime())
					{
						for(ServerWorld server : this.server.getWorlds())
						{
							long i = server.getWorldInfo().getDayTime() + 24000L;
							server.getWorldInfo().setDayTime(i - i % 24000L);
							
							for(PlayerEntity PlayerEntity : server.getPlayers().stream().filter(PlayerEntity::isSleeping).collect(Collectors.toList()))
							{
								PlayerEntity.wakeUp();
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
							KardExo.notifyPlayers(this.server, new TranslationTextComponent("%s is no longer sleeping", player.getDisplayName()));
						}
						
						this.sleep.remove(playername);
					}
				}
			}
		}
	}
}
