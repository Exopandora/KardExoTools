package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;

public class TaskScheduler
{
	private final List<Worker> workers = new ArrayList<Worker>();
	
	public void schedule(AbstractTask task, int offset, TimeUnit offsetUnit, int interval, TimeUnit intervalUnit, int[] warningTimes, TimeUnit warningTimesUnit)
	{
		long offsetMillis = offsetUnit.toMillis(offset);
		long intervalMillis = intervalUnit.toMillis(interval);
		long[] warningTimesMillis = Arrays.stream(warningTimes).mapToObj(warningTimesUnit::toMillis).sorted((a, b) -> Long.compare(b, a)).mapToLong(Long::longValue).toArray();
		
		Worker worker = new Worker(task, offsetMillis, intervalMillis, warningTimesMillis);
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
		private final long[] warningTimes;
		
		public Worker(AbstractTask task, long offset, long interval, long[] warningTimes)
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
			MinecraftServer server = this.task.server;
			
			try
			{
				Thread.sleep(Math.max(0, this.offset - this.warningTimes[0]));
				
				while(server.isRunning())
				{
					long millis = System.currentTimeMillis();
					
					if(this.task.requiresPlayers() && server.getPlayerList().getPlayers().isEmpty())
					{
						server.sendMessage(new TextComponent("Skipping task " + this.task.getName() + " as there are no players on the server"), null);
					}
					else
					{
						for(int x = 0; x < this.warningTimes.length; x++)
						{
							KardExo.broadcastMessage(server, new TextComponent(this.task.getWarningMessage(TimeUnit.MILLISECONDS.toSeconds(this.warningTimes[x]))));
							long waitTime = this.warningTimes[x];
							
							if(x < this.warningTimes.length - 1)
							{
								waitTime -= this.warningTimes[x + 1];
							}
							
							Thread.sleep(waitTime);
						}
						
						this.task.execute();
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
