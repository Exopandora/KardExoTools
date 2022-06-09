package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ShutdownTask implements ITask
{
	@Override
	public void execute(MinecraftServer server)
	{
		if(server.getPlayerList() != null)
		{
			List<ServerPlayer> players = new ArrayList<ServerPlayer>(server.getPlayerList().getPlayers());
			
			for(ServerPlayer player : players)
			{
				player.connection.disconnect(Component.translatable(KardExo.CONFIG.getData().getShutdownMessage()));
			}
		}
		
		server.halt(false);
		
		if(KardExo.CONFIG.getData().isShutdownBackup())
		{
			KardExo.TASK_BACKUP.execute(server);
		}
	}
	
	@Override
	public String getName()
	{
		return "shutdown";
	}
	
	@Override
	public long getOffset()
	{
		return KardExo.CONFIG.getData().getShutdownOffset();
	}
	
	@Override
	public TimeUnit getOffsetTimeUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public long getInterval()
	{
		return -1;
	}
	
	@Override
	public TimeUnit getIntervalTimeUnit()
	{
		return null;
	}
	
	@Override
	public long[] getWarningTimes()
	{
		return KardExo.CONFIG.getData().getShutdownWarningTimes();
	}
	
	@Override
	public TimeUnit getWarningTimesUnit()
	{
		return TimeUnit.MINUTES;
	}
	
	@Override
	public String getWarningMessage(long millis)
	{
		return String.format(KardExo.CONFIG.getData().getShutdownWarningMessage(), this.getWarningTimesUnit().convert(millis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public int getPriority()
	{
		return 150;
	}
	
	@Override
	public boolean isEnabled()
	{
		return KardExo.CONFIG.getData().isShutdownEnabled();
	}
}
