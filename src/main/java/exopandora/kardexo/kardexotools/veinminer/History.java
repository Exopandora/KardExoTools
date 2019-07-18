package exopandora.kardexo.kardexotools.veinminer;

import java.util.Stack;

public class History<T> extends Stack<T>
{
	private static final long serialVersionUID = 6278523913514757437L;
	private final int size;
	
	public History(int size, T entry)
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
		
		return this.push(entry);
	}
}
