package net.kardexo.kardexotools.tasks;

import net.kardexo.kardexotools.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class TaskDispatcher implements ITaskDispatcher
{
	private final MinecraftServer server;
	
	public TaskDispatcher(MinecraftServer server)
	{
		this.server = server;
	}
	
	@Override
	public void dispatch(ITask task)
	{
		this.server.submit(() -> task.execute(this.server));
	}
	
	@Override
	public void warn(ITask task, long millis)
	{
		Util.broadcastMessage(this.server, Component.literal(task.getWarningMessage(millis)));
	}
}
