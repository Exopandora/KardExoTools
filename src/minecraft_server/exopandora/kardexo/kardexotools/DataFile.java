package exopandora.kardexo.kardexotools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class DataFile<T, K>
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	private final File file;
	private final Map<K, T> data = new HashMap<K, T>();
	private final Class<T[]> klass;
	private final Function<T, K> mapper;
	
	public DataFile(String fileName, Class<T[]> klass, Function<T, K> mapper)
	{
		this(new File(fileName), klass, mapper);
	}
	
	public DataFile(File file, Class<T[]> klass, Function<T, K> mapper)
	{
		this.file = file;
		this.klass = klass;
		this.mapper = mapper;
	}
	
	public Map<K, T> getData()
	{
		return this.data;
	}
	
	public void save()
	{
		String data = GSON.toJson(this.data.values());
		
		if(data != null)
		{
			try
			{
				if(!this.file.exists())
				{
					this.file.createNewFile();
				}
				
				Formatter formatter = new Formatter(this.file);
				formatter.format(data);
				formatter.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void read()
	{
		try
		{
			T[] file = DataFile.<T[]>readFile(this.file, this.klass);
			
			this.data.clear();
			
			if(file != null)
			{
				for(T data : file)
				{
					this.data.put(this.mapper.apply(data), data);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static <T> T readFile(File file, Class<T> klass) throws JsonSyntaxException, JsonIOException, FileNotFoundException
	{
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return GSON.<T>fromJson(new FileReader(file), klass);
	}
}
