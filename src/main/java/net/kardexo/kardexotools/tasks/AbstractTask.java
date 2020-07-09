package net.kardexo.kardexotools.tasks;

import net.minecraft.server.MinecraftServer;

public abstract class AbstractTask implements Runnable
{
	private final MinecraftServer server;
	
	public AbstractTask(MinecraftServer server)
	{
		this.server = server;
	}
	
	public MinecraftServer getServer()
	{
		return this.server;
	}
	
	public abstract String getName();
	
	public abstract String getWarningMessage(long seconds);
	
	public abstract boolean requiresPlayers();
	
	public void execute()
	{
		this.server.execute(this);
	}
}
