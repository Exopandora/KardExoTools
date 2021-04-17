package net.kardexo.kardexotools.tasks;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.server.MinecraftServer;

public class TaskSave extends AbstractTask
{
	public TaskSave(MinecraftServer server)
	{
		super(server);
	}
	
	@Override
	public void run()
	{
		KardExo.saveWorlds(this.getServer());
	}
	
	@Override
	public String getName()
	{
		return "save";
	}
	
	@Override
	public String getWarningMessage(long seconds)
	{
		return String.format(KardExo.CONFIG.getSaveWarningMessage(), seconds);
	}
	
	@Override
	public boolean requiresPlayers()
	{
		return false;
	}
}
