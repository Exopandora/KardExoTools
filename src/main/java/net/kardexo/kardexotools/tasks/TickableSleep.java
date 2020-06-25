package net.kardexo.kardexotools.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;

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
		ServerWorld overworld = this.server.getWorld(World.field_234918_g_);
		
		if(!overworld.getPlayers().isEmpty())
		{
			for(PlayerEntity player : overworld.getPlayers())
			{
				String playername = player.getGameProfile().getName();
				
				if(player.isSleeping())
				{
					if(!this.sleep.containsKey(playername) && !overworld.isDaytime())
					{
						KardExo.notifyPlayers(this.server, new TranslationTextComponent("%s is now sleeping", player.getDisplayName()));
						this.sleep.put(playername, overworld.getDayTime());
					}
					
					if(this.sleep.get(playername) + 100 <= overworld.getWorldInfo().getDayTime() && overworld.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
					{
						for(ServerWorld server : this.server.getWorlds())
						{
							long time = server.getWorldInfo().getDayTime() + 24000L;
							server.func_241114_a_(time - time % 24000L);
							
							for(PlayerEntity PlayerEntity : server.getPlayers().stream().filter(PlayerEntity::isSleeping).collect(Collectors.toList()))
							{
								PlayerEntity.wakeUp();
							}
							
							if(overworld.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE))
				            {
								IServerWorldInfo info = (IServerWorldInfo) overworld.getWorldInfo();
								info.setRainTime(0);
								info.setRaining(false);
								info.setThunderTime(0);
								info.setThundering(false);
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
