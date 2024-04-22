package net.kardexo.kardexotools.tasks;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShutdownTask implements ITask
{
	private final Supplier<long[]> schedules = Suppliers.memoize(() -> ITask.parseSchedules(KardExo.CONFIG.getData().getShutdownTimes()));
	
	@Override
	public void execute(MinecraftServer server)
	{
		List<ServerPlayer> players = new ArrayList<ServerPlayer>(server.getPlayerList().getPlayers());
		
		for(ServerPlayer player : players)
		{
			player.connection.disconnect(Component.translatable(KardExo.CONFIG.getData().getShutdownMessage()));
		}
		
		server.halt(false);
		
		if(KardExo.CONFIG.getData().isShutdownBackup())
		{
			KardExo.TASK_BACKUP.execute(server);
		}
	}
	
	@Override
	public long[] getSchedules()
	{
		return this.schedules.get();
	}
	
	@Override
	public String getName()
	{
		return "shutdown";
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
	
	@Override
	public boolean isRecurring()
	{
		return false;
	}
}
