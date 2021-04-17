package net.kardexo.kardexotools.tasks;

import java.util.HashMap;
import java.util.Map;

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
		ServerWorld overworld = this.server.getLevel(World.OVERWORLD);
		
		for(PlayerEntity player : overworld.players())
		{
			String playername = player.getGameProfile().getName();
			
			if(player.isSleeping())
			{
				if(!this.sleep.containsKey(playername) && !overworld.isDay())
				{
					KardExo.notifyPlayers(this.server, new TranslationTextComponent("%s is now sleeping", player.getDisplayName()));
					this.sleep.put(playername, overworld.getDayTime());
				}
				
				if(this.sleep.get(playername) + 100 <= overworld.getLevelData().getDayTime() && overworld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
				{
					for(ServerWorld level : this.server.getAllLevels())
					{
						long time = level.getLevelData().getDayTime() + 24000L;
						
						level.setDayTime(time - time % 24000L);
						level.players().stream().filter(PlayerEntity::isSleeping).forEach(p -> p.stopSleepInBed(false, false));
						
						if(overworld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
			            {
							IServerWorldInfo info = (IServerWorldInfo) overworld.getLevelData();
							info.setRainTime(0);
							info.setRaining(false);
							info.setThunderTime(0);
							info.setThundering(false);
			            }
					}
					
					this.sleep.clear();
				}
			}
			else if(this.sleep.containsKey(playername))
			{
				if(!overworld.isDay())
				{
					KardExo.notifyPlayers(this.server, new TranslationTextComponent("%s is no longer sleeping", player.getDisplayName()));
				}
				
				this.sleep.remove(playername);
			}
		}
	}
}
