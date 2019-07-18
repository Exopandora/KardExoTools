package exopandora.kardexo.kardexotools.veinminer;

import java.util.HashMap;
import java.util.Map;

public class PlayerHistory<T>
{
	private final Map<String, History<T>> history = new HashMap<String, History<T>>();
	private final int size;
	
	public PlayerHistory(int size)
	{
		this.size = size;
	}
	
	public History<T> getHistory(String player)
	{
		return this.history.get(player);
	}
	
	public void add(String player, T entry)
	{
		if(this.history.containsKey(player))
		{
			this.history.get(player).push(entry);
		}
		else
		{
			this.history.put(player, new History<T>(this.size, entry));
		}
	}
	
	public History<T> remove(String player)
	{
		return this.history.remove(player);
	}
	
	public boolean hasUndo(String player)
	{
		return this.history.containsKey(player);
	}
}
