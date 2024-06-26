package net.kardexo.kardexotools.config;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MapFile<K, V> extends ConfigFile<Map<K, V>>
{
	public MapFile(File file, TypeToken<Map<K, V>> typeToken)
	{
		super(file, typeToken, new HashMap<K, V>());
	}
	
	@Override
	public void setData(Map<K, V> data)
	{
		this.data.clear();
		this.data.putAll(data);
	}
	
	public V get(K key)
	{
		return this.data.get(key);
	}
	
	public V put(K key, V value)
	{
		return this.data.put(key, value);
	}
	
	public V remove(K key)
	{
		return this.data.remove(key);
	}
	
	public boolean containsKey(String key)
	{
		return this.data.containsKey(key);
	}
}
