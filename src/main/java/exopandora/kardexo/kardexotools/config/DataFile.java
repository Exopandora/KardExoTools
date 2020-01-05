package exopandora.kardexo.kardexotools.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.ResourceLocation;

public class DataFile<T, K>
{
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.disableHtmlEscaping()
			.setPrettyPrinting()
			.create();
	
	private final File file;
	private final Map<K, T> data = new HashMap<K, T>();
	private final Class<T[]> klass;
	private final Function<T, K> mapper;
	private final Consumer<Collection<T>> initial; 
	
	public DataFile(String fileName, Class<T[]> klass, Function<T, K> mapper)
	{
		this(fileName, klass, mapper, null);
	}
	
	public DataFile(String fileName, Class<T[]> klass, Function<T, K> mapper, Consumer<Collection<T>> initial)
	{
		this(new File(fileName), klass, mapper, initial);
	}
	
	public DataFile(File file, Class<T[]> klass, Function<T, K> mapper)
	{
		this(file, klass, mapper, null);
	}
	
	public DataFile(File file, Class<T[]> klass, Function<T, K> mapper, Consumer<Collection<T>> initial)
	{
		this.file = file;
		this.klass = klass;
		this.mapper = mapper;
		this.initial = initial;
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
	
	@SuppressWarnings("unchecked")
	public void read() throws Exception
	{
		T[] file = DataFile.<T[]>readFile(this.file, this.klass);
		boolean flag = false;
		this.data.clear();
		
		if(file == null)
		{
			if(this.initial != null)
			{
				Set<T> initial = new HashSet<T>();
				this.initial.accept(initial);
				
				if(initial != null)
				{
					file = (T[]) initial.toArray();
				}
			}
			
			flag = true;
		}
		
		if(file != null)
		{
			for(T data : file)
			{
				K key = this.mapper.apply(data);
				
				if(key != null)
				{
					this.data.put(key, data);
				}
			}
		}
		
		if(flag)
		{
			this.save();
		}
	}
	
	private static <T> T readFile(File file, Class<T> klass) throws JsonSyntaxException, JsonIOException, FileNotFoundException
	{
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
				return null;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return GSON.<T>fromJson(new FileReader(file), klass);
	}
}
