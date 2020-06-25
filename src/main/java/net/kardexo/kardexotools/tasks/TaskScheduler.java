package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.util.text.StringTextComponent;

public class TaskScheduler
{
	private final List<Worker> workers = new ArrayList<Worker>();
	
	public void schedule(AbstractTask task, long offset, long interval, int[] warningTimes)
	{
		Worker worker = new Worker(task, offset, interval, warningTimes);
		this.workers.add(worker);
		worker.start();
	}
	
	public void stop()
	{
		for(Worker worker : this.workers)
		{
			worker.interrupt();
		}
	}
	
	public static class Worker extends Thread
	{
		private final AbstractTask task;
		private final long offset;
		private final long interval;
		private final int[] warningTimes;
		
		public Worker(AbstractTask task, long offset, long interval, int[] warningTimes)
		{
			super("KardExo/" + task.getName());
			this.task = task;
			this.offset = offset;
			this.interval = interval;
			this.warningTimes = warningTimes;
		}
		
		@Override
		public void run()
		{
			try
			{
				Thread.sleep(Math.max(0, this.offset - this.warningTimes[0]));
				
				while(this.task.getServer().isServerRunning())
				{
					long millis = System.currentTimeMillis();
					
					if(this.task.requiresPlayers() && this.task.getServer().getPlayerList().getPlayers().isEmpty())
					{
						this.task.getServer().sendMessage(new StringTextComponent("Skipping task " + this.task.getName() + " as there are no players on the server"), null);
					}
					else
					{
						for(int x = 0; x < this.warningTimes.length; x++)
						{
							KardExo.notifyPlayers(this.task.getServer(), new StringTextComponent(this.task.getWarningMessage(TimeUnit.MILLISECONDS.toSeconds(this.warningTimes[x]))));
							
							long waitTime = this.warningTimes[x];
							
							if(x < this.warningTimes.length - 1)
							{
								waitTime -= this.warningTimes[x + 1];
							}
							
							Thread.sleep(waitTime);
						}
						
						this.task.getServer().execute(this.task);
					}
					
					Thread.sleep(Math.max(0, this.interval - this.warningTimes[0] - (System.currentTimeMillis() - millis)));
				}
			}
			catch(InterruptedException e)
			{
				KardExo.LOGGER.info("Stopped task " + this.task.getName());
			}
		}
	}
}
