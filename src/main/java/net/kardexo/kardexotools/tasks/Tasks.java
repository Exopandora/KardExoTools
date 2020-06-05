package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.Config;

public class Tasks
{	
	public static final Runnable SAVE = KardExo::saveWorlds;
	public static final Runnable BACKUP = new RunnableBackup();
	
	private static final ScheduledTask SCHEDULED_SAVE = new ScheduledTask("save", Config.OFFSET_SAVE, Config.INTERVAL_SAVE, false, SAVE, Config.WARNING_MESSAGE_SAVE, Config.WARNING_TIMES_SAVE);
	private static final ScheduledTask SCHEDULED_BACKUP = new ScheduledTask("backup", Config.OFFSET_BACKUP, Config.INTERVAL_BACKUP, true, BACKUP, Config.WARNING_MESSAGE_BACKUP, Config.WARNING_TIMES_BACKUP);
	
	private static final List<ScheduledTask> TASKS = new ArrayList<ScheduledTask>();
	private static boolean RUNNING;
	
	static
	{
		Tasks.add(Tasks.SCHEDULED_SAVE);
		Tasks.add(Tasks.SCHEDULED_BACKUP);
	}
	
	public static void start()
	{
		if(!Tasks.RUNNING)
		{
			for(ScheduledTask task : Tasks.TASKS)
			{
				task.start();
			}
			
			Tasks.RUNNING = true;
		}
	}
	
	public static void stop()
	{
		if(Tasks.RUNNING)
		{
			for(ScheduledTask task : Tasks.TASKS)
			{
				task.interrupt();
			}
			
			Tasks.RUNNING = false;
		}
	}
	
	private static void add(ScheduledTask task)
	{
		if(!Tasks.TASKS.contains(task))
		{
			Tasks.TASKS.add(task);
		}
		else
		{
			KardExo.LOGGER.error("Duplicate task " + task.getTaskName());
		}
	}
}
