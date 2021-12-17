package net.kardexo.kardexotools.tasks;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import net.minecraft.server.MinecraftServer;

public interface ITask
{
	void execute(MinecraftServer server);
	
	String getName();
	
	long getOffset();
	
	TimeUnit getOffsetTimeUnit();
	
	long getInterval();
	
	TimeUnit getIntervalTimeUnit();
	
	long[] getWarningTimes();
	
	TimeUnit getWarningTimesUnit();
	
	String getWarningMessage(long millis);
	
	int getPriority();
	
	boolean isEnabled();
	
	default long getOffsetMillis()
	{
		return this.getOffsetTimeUnit().toMillis(this.getOffset());
	}
	
	default long getIntervalMillis()
	{
		return this.getIntervalTimeUnit().toMillis(this.getInterval());
	}
	
	default long[] getWarningTimesMillis()
	{
		return Arrays.stream(this.getWarningTimes()).mapToObj(this.getWarningTimesUnit()::toMillis).sorted((a, b) -> Long.compare(b, a)).mapToLong(Long::longValue).toArray();
	}
}
