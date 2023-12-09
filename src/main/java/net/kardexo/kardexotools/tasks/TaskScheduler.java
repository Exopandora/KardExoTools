package net.kardexo.kardexotools.tasks;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.kardexo.kardexotools.KardExo;

public class TaskScheduler extends Thread
{
	private final List<ITask> tasks = new ArrayList<ITask>();
	private final ITaskDispatcher dispatcher;
	
	public TaskScheduler(ITaskDispatcher dispatcher)
	{
		this.dispatcher = dispatcher;
	}
	
	@Override
	public void run()
	{
		try
		{
			List<Event> events = new ArrayList<Event>();
			long timestamp = System.currentTimeMillis();
			
			for(ITask task : this.tasks)
			{
				if(task.isEnabled())
				{
					events.add(firstEventForTaskAfter(task, timestamp));
				}
			}
			
			Collections.sort(events);
			
			while(!events.isEmpty())
			{
				while(resolveCollisions(events));
				
				Event event = events.remove(0);
				ITask task = event.task();
				long sleep = Math.max(0, event.timestamp() - System.currentTimeMillis());
				
				if(sleep > 0)
				{
					Thread.sleep(sleep);
				}
				
				if(event.state() == 0)
				{
					this.dispatcher.dispatch(task);
					
					if(task.isRecurring())
					{
						events.add(firstEventForTaskAfter(task, event.timestamp()));
						Collections.sort(events);
					}
				}
				else
				{
					long[] times = task.getWarningTimesMillis();
					long current = times[times.length - event.state()];
					long waitingTime = current;
					
					if(event.state() > 1)
					{
						waitingTime -= times[times.length - event.state() + 1];
					}
					
					this.dispatcher.warn(task, current);
					events.add(new Event(task, event.timestamp() + waitingTime, event.state() - 1));
					Collections.sort(events);
				}
			}
		}
		catch(InterruptedException e)
		{
			KardExo.LOGGER.info("Stopped tasks");
		}
	}
	
	public void registerTask(ITask task)
	{
		this.tasks.add(task);
	}
	
	private static boolean resolveCollisions(List<Event> events)
	{
		for(int x = 0; x < events.size(); x++)
		{
			Event a = events.get(x);
			long timeA = getExecutionTime(a);
			
			for(int y = x + 1; y < events.size(); y++)
			{
				Event b = events.get(y);
				long timeB = getExecutionTime(b);
				
				if(timeA == timeB)
				{
					ITask task;
					
					if(a.task().getPriority() < b.task().getPriority())
					{
						task = a.task();
						events.remove(x);
					}
					else
					{
						task = b.task();
						events.remove(y);
					}
					
					if(task.isRecurring())
					{
						events.add(firstEventForTaskAfter(task, timeA));
						Collections.sort(events);
					}
					
					return true;
				}
				else if(timeA < timeB)
				{
					break;
				}
			}
		}
		
		return false;
	}
	
	private static long getExecutionTime(Event event)
	{
		if(event.state() > 0)
		{
			long[] times = event.task().getWarningTimesMillis();
			return event.timestamp() + times[times.length - event.state()];
		}
		
		return event.timestamp();
	}
	
	private static Event firstEventForTaskAfter(ITask task, long timestamp)
	{
		long executionTime = nextExecutionTime(task, timestamp);
		int initialState = initialState(task, executionTime - timestamp);
		long time = executionTime - maxWarningDurationMillis(task, initialState);
		return new Event(task, time, initialState);
	}
	
	private static long nextExecutionTime(ITask task, long timestamp)
	{
		ZoneId zoneId = ZoneOffset.systemDefault();
		LocalDateTime last = Instant.ofEpochMilli(timestamp)
			.atZone(zoneId)
			.toLocalDateTime();
		LocalDateTime startOfDay = last.with(LocalTime.MIN);
		
		for(long offset : task.getSchedules())
		{
			LocalDateTime next = startOfDay.plus(offset, ChronoUnit.MILLIS);
			
			if(next.compareTo(last) == 1)
			{
				return toMillis(next, zoneId);
			}
		}
		
		return toMillis(startOfDay.plusDays(1).plus(task.getSchedules()[0], ChronoUnit.MILLIS), zoneId);
	}
	
	private static long toMillis(LocalDateTime localDateTime, ZoneId zoneId)
	{
		return ZonedDateTime.of(localDateTime, zoneId).toInstant().toEpochMilli();
	}
	
	private static int initialState(ITask task, long waitTime)
	{
		long[] warningTimes = task.getWarningTimesMillis();
		int state = warningTimes.length;
		
		while(state > 0 && warningTimes[warningTimes.length - state] > waitTime)
		{
			state--;
		}
		
		return state;
	}
	
	private static long maxWarningDurationMillis(ITask task, int state)
	{
		if(task.getWarningTimes().length == 0 || state == 0)
		{
			return 0;
		}
		
		return task.getWarningTimesMillis()[task.getWarningTimes().length - state];
	}
	
	private record Event(ITask task, long timestamp, int state) implements Comparable<Event>
	{
		@Override
		public int compareTo(Event event)
		{
			int result = Long.compare(this.timestamp(), event.timestamp());
			
			if(result == 0)
			{
				return Integer.compare(event.task().getPriority(), this.task().getPriority());
			}
			
			return result;
		}
	}
}
