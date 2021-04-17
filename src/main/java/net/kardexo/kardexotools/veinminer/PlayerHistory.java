package net.kardexo.kardexotools.veinminer;

import java.util.HashMap;
import java.util.Map;

public class PlayerHistory<T>
{
	private final Map<String, LimitedStack<T>> history = new HashMap<String, LimitedStack<T>>();
	private final int size;
	
	public PlayerHistory(int size)
	{
		this.size = size;
	}
	
	public T peek(String player)
	{
		return this.history.get(player).peek();
	}
	
	public T pop(String player)
	{
		LimitedStack<T> stack = this.history.get(player);
		T result = stack.pop();
		
		if(stack.isEmpty())
		{
			this.history.remove(player);
		}
		
		return result;
	}
	
	public void add(String player, T entry)
	{
		this.history.computeIfAbsent(player, key -> new LimitedStack<T>(this.size)).push(entry);
	}
	
	public LimitedStack<T> remove(String player)
	{
		return this.history.remove(player);
	}
	
	public boolean hasUndo(String player)
	{
		return this.history.containsKey(player);
	}
}
