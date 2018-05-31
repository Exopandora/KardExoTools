package exopandora.kardexo.kardexotools;

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
