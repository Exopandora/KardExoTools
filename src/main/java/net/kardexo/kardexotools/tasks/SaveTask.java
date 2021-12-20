package net.kardexo.kardexotools.tasks;

import java.util.concurrent.TimeUnit;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.util.Util;
import net.minecraft.server.MinecraftServer;

public class SaveTask implements ITask
{
	@Override
	public void execute(MinecraftServer server)
	{
		Util.saveLevels(server);
	}
	
	@Override
	public String getName()
	{
		return "save";
	}
	
	@Override
	public long getOffset()
	{
		return KardExo.CONFIG.getSaveOffset();
	}
	
	@Override
	public TimeUnit getOffsetTimeUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public long getInterval()
	{
		return KardExo.CONFIG.getSaveInterval();
	}
	
	@Override
	public TimeUnit getIntervalTimeUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public long[] getWarningTimes()
	{
		return KardExo.CONFIG.getSaveWarningTimes();
	}
	
	@Override
	public TimeUnit getWarningTimesUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public String getWarningMessage(long millis)
	{
		return String.format(KardExo.CONFIG.getSaveWarningMessage(), this.getWarningTimesUnit().convert(millis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public int getPriority()
	{
		return 50;
	}
	
	@Override
	public boolean isEnabled()
	{
		return KardExo.CONFIG.isSaveEnabled();
	}
}
