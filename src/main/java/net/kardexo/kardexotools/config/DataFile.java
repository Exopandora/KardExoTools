package net.kardexo.kardexotools.config;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataFile<T, K> extends AbstractConfigFile<T[]>
{
	private final Function<T, K> keyMapper;
	private final Map<K, T> map = new HashMap<K, T>();
	
	public DataFile(File file, Class<T[]> klass, Function<T, K> mapper)
	{
		this(file, klass, mapper, null);
	}
	
	public DataFile(File file, Class<T[]> klass, Function<T, K> keyMapper, Supplier<Collection<T>> initial)
	{
		super(file, klass);
		this.keyMapper = keyMapper;
		
		if(initial != null)
		{
			this.setData(DataFile.collectionToArray(initial));
		}
	}
	
	@Override
	protected T[] getData()
	{
		return DataFile.collectionToArray(this.map.values());
	}
	
	@Override
	protected void setData(T[] data)
	{
		this.map.clear();
		
		for(T item : data)
		{
			K key = this.keyMapper.apply(item);
			
			if(key != null)
			{
				this.map.put(key, item);
			}
		}
	}
	
	public Map<K, T> getMap()
	{
		return this.map;
	}
	
	public T get(K key)
	{
		return this.map.get(key);
	}
	
	public T put(K key, T value)
	{
		return this.map.put(key, value);
	}
	
	public T remove(K key)
	{
		return this.map.remove(key);
	}
	
	public boolean containsKey(String key)
	{
		return this.map.containsKey(key);
	}
	
	private static <T> T[] collectionToArray(Supplier<Collection<T>> supplier)
	{
		if(supplier != null)
		{
			collectionToArray(supplier.get());
		}
		
		return collectionToArray(Collections.emptyList());
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T[] collectionToArray(Collection<T> collection)
	{
		if(collection != null)
		{
			return (T[]) collection.toArray();
		}
		
		return null;
	}
}
