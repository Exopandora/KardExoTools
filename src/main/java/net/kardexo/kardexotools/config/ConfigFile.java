package net.kardexo.kardexotools.config;

import java.io.File;
import java.util.function.Supplier;

public class ConfigFile<T> extends AbstractConfigFile<T>
{
	private T data;
	
	public ConfigFile(File file, Class<T> klass)
	{
		this(file, klass, null);
	}
	
	public ConfigFile(File file, Class<T> klass, Supplier<T> initial)
	{
		super(file, klass);
		
		if(initial != null)
		{
			this.setData(initial.get());
		}
	}
	
	@Override
	public T getData()
	{
		return this.data;
	}
	
	@Override
	public void setData(T data)
	{
		this.data = data;
	}
}
