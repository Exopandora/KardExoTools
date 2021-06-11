package net.kardexo.kardexotools.tasks;

import java.util.HashMap;
import java.util.Map;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

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
		ServerLevel overworld = this.server.getLevel(Level.OVERWORLD);
		
		for(Player player : overworld.players())
		{
			String playername = player.getGameProfile().getName();
			
			if(player.isSleeping())
			{
				if(!this.sleep.containsKey(playername) && !overworld.isDay())
				{
					KardExo.notifyPlayers(this.server, new TranslatableComponent("%s is now sleeping", player.getDisplayName()));
					this.sleep.put(playername, overworld.getDayTime());
				}
				
				if(this.sleep.get(playername) + 100 <= overworld.getLevelData().getDayTime() && overworld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
				{
					for(ServerLevel level : this.server.getAllLevels())
					{
						long time = level.getLevelData().getDayTime() + 24000L;
						
						level.setDayTime(time - time % 24000L);
						level.players().stream().filter(Player::isSleeping).forEach(p -> p.stopSleepInBed(false, false));
						
						if(overworld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
			            {
							ServerLevelData data = (ServerLevelData) overworld.getLevelData();
							data.setRainTime(0);
							data.setRaining(false);
							data.setThunderTime(0);
							data.setThundering(false);
			            }
					}
					
					this.sleep.clear();
				}
			}
			else if(this.sleep.containsKey(playername))
			{
				if(!overworld.isDay())
				{
					KardExo.notifyPlayers(this.server, new TranslatableComponent("%s is no longer sleeping", player.getDisplayName()));
				}
				
				this.sleep.remove(playername);
			}
		}
	}
}
