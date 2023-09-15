package net.kardexo.kardexotools.tasks;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.util.Util;
import net.minecraft.server.MinecraftServer;

public class SaveTask implements ITask
{
	private final Supplier<long[]> schedules = Suppliers.memoize(() -> ITask.parseSchedules(KardExo.CONFIG.getData().getSaveTimes()));
	
	@Override
	public void execute(MinecraftServer server)
	{
		Util.saveLevels(server);
	}
	
	@Override
	public long[] getSchedules()
	{
		return this.schedules.get();
	}
	
	@Override
	public String getName()
	{
		return "save";
	}
	
	@Override
	public long[] getWarningTimes()
	{
		return KardExo.CONFIG.getData().getSaveWarningTimes();
	}
	
	@Override
	public TimeUnit getWarningTimesUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public String getWarningMessage(long millis)
	{
		return String.format(KardExo.CONFIG.getData().getSaveWarningMessage(), this.getWarningTimesUnit().convert(millis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public int getPriority()
	{
		return 50;
	}
	
	@Override
	public boolean isEnabled()
	{
		return KardExo.CONFIG.getData().isSaveEnabled();
	}
	
	@Override
	public boolean isRecurring()
	{
		return true;
	}
}
