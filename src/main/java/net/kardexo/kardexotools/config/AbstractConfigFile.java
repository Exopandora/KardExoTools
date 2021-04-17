package net.kardexo.kardexotools.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractConfigFile<T>
{
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
		.registerTypeAdapter(BlockPos.class, new BlockPosTypeAdapter())
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();
	
	private final File file;
	private final Class<T> klass;
	
	public AbstractConfigFile(File file, Class<T> klass)
	{
		this.file = file;
		this.klass = klass;
	}
	
	protected abstract T getData();
	protected abstract void setData(T data);
	
	public void save()
	{
		try
		{
			FileWriter writer = new FileWriter(this.file);
			GSON.toJson(this.getData(), writer);
			writer.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void read()
	{
		if(this.file.exists())
		{
			try
			{
				T data = GSON.fromJson(new FileReader(this.file), this.klass);
				
				if(data != null)
				{
					this.setData(data);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			this.save();
		}
	}
	
	private static class BlockPosTypeAdapter implements JsonDeserializer<BlockPos>, JsonSerializer<BlockPos>
	{
		@Override
		public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject object = json.getAsJsonObject();
			
			int x = object.get("x").getAsInt();
			int y = object.get("y").getAsInt();
			int z = object.get("z").getAsInt();
			
			return new BlockPos(x, y, z);
		}
		
		@Override
		public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject json = new JsonObject();
			
			json.addProperty("x", src.getX());
			json.addProperty("y", src.getY());
			json.addProperty("z", src.getZ());
			
			return json;
		}
	}
}
