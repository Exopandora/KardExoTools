package net.kardexo.kardexotools.veinminer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHistory<T>
{
	private final Map<UUID, LimitedStack<T>> history = new HashMap<UUID, LimitedStack<T>>();
	private final int size;
	
	public PlayerHistory(int size)
	{
		this.size = size;
	}
	
	public T peek(UUID uuid)
	{
		return this.history.get(uuid).peek();
	}
	
	public T pop(UUID uuid)
	{
		LimitedStack<T> stack = this.history.get(uuid);
		T result = stack.pop();
		
		if(stack.isEmpty())
		{
			this.history.remove(uuid);
		}
		
		return result;
	}
	
	public void add(UUID uuid, T entry)
	{
		this.history.computeIfAbsent(uuid, key -> new LimitedStack<T>(this.size)).push(entry);
	}
	
	public LimitedStack<T> remove(UUID uuid)
	{
		return this.history.remove(uuid);
	}
	
	public boolean hasUndo(UUID uuid)
	{
		return this.history.containsKey(uuid);
	}
}
