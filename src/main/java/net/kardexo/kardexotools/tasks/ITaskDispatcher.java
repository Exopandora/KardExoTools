package net.kardexo.kardexotools.tasks;

public interface ITaskDispatcher
{
	void dispatch(ITask task);
	
	void warn(ITask task, long millis);
}
