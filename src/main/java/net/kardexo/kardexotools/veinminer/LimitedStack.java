package net.kardexo.kardexotools.veinminer;

import java.util.Stack;

@SuppressWarnings("serial")
public class LimitedStack<T> extends Stack<T>
{
	private final int size;
	
	public LimitedStack(int size)
	{
		this.size = size;
	}
	
	@Override
	public T push(T entry)
	{
		if(this.size() == this.size)
		{
			this.remove(0);
		}
		
		return super.push(entry);
	}
}
