package exopandora.kardexo.kardexotools.tasks;

import exopandora.kardexo.kardexotools.data.Config;

public class ShutdownHook extends Thread
{
	public ShutdownHook()
	{
		super("KardExo Shutdown Thread");
	}
	
	@Override
	public void run()
	{
		Tasks.stop();
		Config.saveAllFiles();
	}
}
