package net.kardexo.kardexotools.veinminer;

import java.util.Stack;

public class LimitedStack<T> extends Stack<T>
{
	private static final long serialVersionUID = 6278523913514757437L;
	private final int size;
	
	public LimitedStack(int size, T entry)
	{
		this.size = size;
		this.push(entry);
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