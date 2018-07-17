package exopandora.kardexo.kardexotools;

import java.util.concurrent.TimeUnit;

import net.minecraft.util.text.TextComponentString;

public class ScheduledTask extends Thread
{
	private final String name;
	private final long offset;
	private final long interval;
	private final long delay;
	private final Runnable task;
	private final boolean requiresPlayers;
	private final String warning;
	
	public ScheduledTask(String name, int offset, int interval, boolean requiresPlayers, Runnable task, String warning, int delay)
	{
		super(name);
		this.name = name;
		this.offset = TimeUnit.MINUTES.toMillis(offset);
		this.interval = TimeUnit.MINUTES.toMillis(interval);
		this.task = task;
		this.requiresPlayers = requiresPlayers;
		this.warning = String.format(warning, delay);
		this.delay = TimeUnit.MINUTES.toMillis(delay);
	}
	
	@Override
	public void run()
	{
		try
		{
			Thread.sleep(Math.max(0, this.offset - this.delay));
			
			while(KardExo.getServer().isServerRunning())
			{
				long millis = System.currentTimeMillis();
				
				if(this.requiresPlayers && KardExo.getServer().getPlayerList().getPlayers().isEmpty())
				{
					KardExo.getServer().logInfo("Skipping task " + this.name + " as there are no players on the server");
				}
				else
				{
					KardExo.notifyPlayers(KardExo.getServer(), new TextComponentString(this.warning));
					Thread.sleep(this.delay);
					KardExo.getServer().addScheduledTask(this.task);
				}
				
				Thread.sleep(this.interval - (System.currentTimeMillis() - millis));
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
		if(object != null && object instanceof ScheduledTask)
		{
			return this.name.equals(((ScheduledTask) object).getTaskName());
		}
		
		return false;
	}
}
