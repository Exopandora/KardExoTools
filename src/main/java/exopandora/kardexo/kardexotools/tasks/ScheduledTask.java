package exopandora.kardexo.kardexotools.tasks;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import exopandora.kardexo.kardexotools.KardExo;
import net.minecraft.util.text.StringTextComponent;

public class ScheduledTask extends Thread
{
	private final String name;
	private final long offset;
	private final long interval;
	private final long[] warningTimes;
	private final Runnable task;
	private final boolean requiresPlayers;
	private final String warningMessage;
	
	public ScheduledTask(String name, int offset, int interval, boolean requiresPlayers, Runnable task, String warningMessage, int[] warningTimes)
	{
		super("KardExo/" + name);
		this.name = name;
		this.offset = TimeUnit.MINUTES.toMillis(offset);
		this.interval = TimeUnit.MINUTES.toMillis(interval);
		this.task = task;
		this.requiresPlayers = requiresPlayers;
		this.warningMessage = warningMessage;
		this.warningTimes = Arrays.stream(warningTimes).mapToObj(TimeUnit.SECONDS::toMillis).sorted((a, b) -> Long.compare(b, a)).mapToLong(Long::longValue).toArray();
	}
	
	@Override
	public void run()
	{
		try
		{
			Thread.sleep(Math.max(0, this.offset - this.warningTimes[0]));
			
			while(KardExo.getServer().isServerRunning())
			{
				long millis = System.currentTimeMillis();
				
				if(this.requiresPlayers && KardExo.getServer().func_184103_al().getPlayers().isEmpty())
				{
					KardExo.getServer().logInfo("Skipping task " + this.name + " as there are no players on the server");
				}
				else
				{
					for(int x = 0; x < this.warningTimes.length; x++)
					{
						KardExo.notifyPlayers(KardExo.getServer(), new StringTextComponent(String.format(this.warningMessage, TimeUnit.MILLISECONDS.toSeconds(this.warningTimes[x]))));
						
						long waitTime = this.warningTimes[x];
						
						if(x < this.warningTimes.length - 1)
						{
							waitTime -= this.warningTimes[x + 1];
						}
						
						Thread.sleep(waitTime);
					}
					
					KardExo.getServer().execute(this.task);
				}
				
				Thread.sleep(Math.max(0, this.interval - this.warningTimes[0] - (System.currentTimeMillis() - millis)));
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
