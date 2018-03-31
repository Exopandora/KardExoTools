package exopandora.kardexo.kardexotools;

import java.util.concurrent.TimeUnit;

import net.minecraft.util.text.TextComponentString;

public class ScheduledTask extends Thread
{
	private final String name;
	private final int offset;
	private final int interval;
	private final int delay;
	private final Runnable task;
	private final boolean requiresPlayers;
	private final String warning;
	private final TimeUnit unit;
	
	public ScheduledTask(String name, int offset, int interval, boolean requiresPlayers, Runnable task, String warning, int delay, TimeUnit unit)
	{
		this.name = name;
		this.offset = offset;
		this.interval = interval;
		this.task = task;
		this.requiresPlayers = requiresPlayers;
		this.warning = String.format(warning, delay);
		this.delay = delay;
		this.unit = unit;
	}
	
	@Override
	public void run()
	{
		try
		{
			Thread.sleep(TimeUnit.MINUTES.toMillis(this.offset));
			
			while(KardExo.getServer().isServerRunning())
			{
				long millis = System.currentTimeMillis();
				
				if(this.requiresPlayers && KardExo.getServer().getPlayerList().getPlayerList().isEmpty())
				{
					KardExo.getServer().logInfo("Skipping task " + this.name + " as there are no players on the server");
				}
				else
				{
					KardExo.notifyPlayers(KardExo.getServer(), new TextComponentString(this.warning));
					Thread.sleep(this.unit.toMillis(this.delay));
					KardExo.getServer().addScheduledTask(this.task);
				}
				
				Thread.sleep(TimeUnit.MINUTES.toMillis(this.interval) - (System.currentTimeMillis() - millis));
			}
		}
		catch(InterruptedException e)
		{
			KardExo.LOGGER.info("Stopped task " + this.name);
		}
	}
	
	public String getTaskName()
	{
		return this.name;
	}
	
	public Runnable getTask()
	{
		return this.task;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object instanceof ScheduledTask)
		{
			ScheduledTask task = (ScheduledTask) object;
			
			return this.name.equals(task.getTaskName());
		}
		
		return false;
	}
}
