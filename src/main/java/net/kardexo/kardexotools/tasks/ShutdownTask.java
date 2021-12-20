package net.kardexo.kardexotools.tasks;

import java.util.concurrent.TimeUnit;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.server.MinecraftServer;

public class ShutdownTask implements ITask
{
	@Override
	public void execute(MinecraftServer server)
	{
		server.halt(false);
		
		if(KardExo.CONFIG.isShutdownBackup())
		{
			server.execute(() -> KardExo.TASK_BACKUP.execute(server));
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
		return KardExo.CONFIG.getShutdownOffset();
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
		return KardExo.CONFIG.getShutdownWarningTimes();
	}
	
	@Override
	public TimeUnit getWarningTimesUnit()
	{
		return TimeUnit.MINUTES;
	}
	
	@Override
	public String getWarningMessage(long millis)
	{
		return String.format(KardExo.CONFIG.getShutdownWarningMessage(), this.getWarningTimesUnit().convert(millis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public int getPriority()
	{
		return 150;
	}
	
	@Override
	public boolean isEnabled()
	{
		return KardExo.CONFIG.isShutdownEnabled();
	}
}
