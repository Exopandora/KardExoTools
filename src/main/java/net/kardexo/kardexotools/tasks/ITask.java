package net.kardexo.kardexotools.tasks;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.server.MinecraftServer;

public interface ITask
{
	static Pattern SCHEDULE_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
	
	void execute(MinecraftServer server);
	
	String getName();
	
	long[] getSchedules();
	
	long[] getWarningTimes();
	
	TimeUnit getWarningTimesUnit();
	
	String getWarningMessage(long millis);
	
	int getPriority();
	
	boolean isEnabled();
	
	boolean isRecurring();
	
	default long[] getWarningTimesMillis()
	{
		return Arrays.stream(this.getWarningTimes())
			.mapToObj(this.getWarningTimesUnit()::toMillis)
			.sorted((a, b) -> Long.compare(b, a))
			.mapToLong(Long::longValue)
			.toArray();
	}
	
	static long[] parseSchedules(String[] schedules)
	{
		long[] offsets = new long[schedules.length];
		
		for(int x = 0; x < schedules.length; x++)
		{
			Matcher matcher = SCHEDULE_PATTERN.matcher(schedules[x]);
			
			if(!matcher.find())
			{
				throw new IllegalArgumentException("Invalid time format \"" + schedules[x] + "\"");
			}
			
			int hours = Integer.parseInt(matcher.group(1));
			int minutes = Integer.parseInt(matcher.group(2));
			
			if(hours > 23)
			{
				throw new IllegalArgumentException("Invalid hour \"" + hours + "\". Must be at most 23.");
			}
			
			if(minutes > 59)
			{
				throw new IllegalArgumentException("Invalid minute \"" + minutes + "\". Must be at most 59.");
			}
			
			offsets[x] = (hours * 3600L + minutes * 60L) * 1000L;
		}
		
		return offsets;
	}
}
