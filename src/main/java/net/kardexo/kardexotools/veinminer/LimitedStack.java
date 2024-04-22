package net.kardexo.kardexotools.veinminer;

import java.util.Stack;

public class LimitedStack<T> extends Stack<T>
{
	private final int limit;
	
	public LimitedStack(int limit)
	{
		this.limit = limit;
	}
	
	@Override
	public T push(T entry)
	{
		if(this.size() == this.limit)
		{
			this.remove(0);
		}
		
		return super.push(entry);
	}
}
