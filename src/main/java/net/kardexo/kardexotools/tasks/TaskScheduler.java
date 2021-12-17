package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
			Map<ITask, Integer> states = new HashMap<ITask, Integer>();
			ArrayList<Event> events = new ArrayList<Event>();
			long time = System.currentTimeMillis();
			
			for(ITask task : this.tasks)
			{
				if(task.isEnabled())
				{
					int initialState = this.initialState(task, task.getOffsetMillis());
					states.put(task, initialState);
					events.add(new Event(task, time + task.getOffsetMillis() - this.getMaxWarningDurationMillis(task, initialState)));
				}
			}
			
			Collections.sort(events);
			
			while(!events.isEmpty())
			{
				while(this.reschedule(events, states));
				
				Event event = events.remove(0);
				ITask task = event.task();
				int state = states.get(task);
				long sleep = Math.max(0, event.timestamp() - System.currentTimeMillis());
				
				if(sleep > 0)
				{
					Thread.sleep(sleep);
				}
				
				if(state == 0)
				{
					this.dispatcher.dispatch(task);
					
					if(task.getInterval() > 0)
					{
						int newState = this.initialState(task, task.getIntervalMillis());
						states.put(task, newState);
						events.add(new Event(task, event.timestamp() + task.getIntervalMillis() - this.getMaxWarningDurationMillis(task, newState)));
						Collections.sort(events);
					}
					else
					{
						states.remove(task);
					}
				}
				else
				{
					long[] times = task.getWarningTimesMillis();
					long current = times[times.length - state];
					long waitingTime = current;
					
					if(state > 1)
					{
						waitingTime -= times[times.length - state + 1];
					}
					
					this.dispatcher.warn(task, task.getWarningTimesUnit().convert(current, TimeUnit.MILLISECONDS));
					states.put(task, state - 1);
					events.add(new Event(task, event.timestamp() + waitingTime));
					Collections.sort(events);
				}
			}
		}
		catch(InterruptedException e)
		{
			KardExo.LOGGER.info("Stopped tasks");
		}
	}
	
	private boolean reschedule(ArrayList<Event> events, Map<ITask, Integer> states)
	{
		for(int x = 0; x < events.size(); x++)
		{
			Event a = events.get(x);
			long timeA = this.getExecutionTime(a, states);
			
			for(int y = x + 1; y < events.size(); y++)
			{
				Event b = events.get(y);
				long timeB = this.getExecutionTime(b, states);
				
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
					
					if(task.getInterval() > 0)
					{
						int newState = this.initialState(task, task.getIntervalMillis());
						states.put(task, newState);
						events.add(new Event(task, timeA + task.getIntervalMillis() - this.getMaxWarningDurationMillis(task, newState)));
						Collections.sort(events);
					}
					else
					{
						states.remove(task);
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
	
	private long getExecutionTime(Event event, Map<ITask, Integer> states)
	{
		int state = states.get(event.task());
		
		if(state > 0)
		{
			long[] times = event.task().getWarningTimesMillis();
			return event.timestamp() + times[times.length - state];
		}
		
		return event.timestamp();
	}
	
	private int initialState(ITask task, long waitTime)
	{
		long[] warningTimes = task.getWarningTimesMillis();
		int state = warningTimes.length;
		
		while(state > 0 && warningTimes[warningTimes.length - state] > waitTime)
		{
			state--;
		}
		
		return state;
	}
	
	private long getMaxWarningDurationMillis(ITask task, int state)
	{
		if(task.getWarningTimes().length == 0 || state == 0)
		{
			return 0;
		}
		
		return task.getWarningTimesMillis()[task.getWarningTimes().length - state];
	}
	
	public void registerTask(ITask task)
	{
		this.tasks.add(task);
	}
	
	private record Event(ITask task, long timestamp) implements Comparable<Event>
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
