package net.kardexo.kardexotools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.util.BlockPredicateWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;

public class ConfigFile<T>
{
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter())
		.registerTypeAdapter(BlockPos.class, new BlockPosTypeAdapter())
		.registerTypeAdapter(BoundingBox.class, new BoundingBoxTypeAdapter())
		.registerTypeAdapter(BlockPredicateWrapper.class, new BlockPredicateWrapper.Serializer())
        .registerTypeHierarchyAdapter(Component.class, new ComponentSerializerAdapter())
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();
	
	private final File file;
	private final TypeToken<T> typeToken;
	protected T data;
	
	public ConfigFile(File file, TypeToken<T> typeToken)
	{
		this(file, typeToken, null);
	}
	
	protected ConfigFile(File file, TypeToken<T> typeToken, T initial)
	{
		this.file = file;
		this.typeToken = typeToken;
		this.data = initial;
	}
	
	public T getData()
	{
		return this.data;
	}
	
	public void setData(T data)
	{
		this.data = data;
	}
	
	public void save()
	{
		KardExo.LOGGER.info("Writing {}", this.file.getName());
		
		try
		{
			FileWriter writer = new FileWriter(this.file);
			GSON.toJson(this.getData(), writer);
			writer.flush();
		}
		catch(Exception e)
		{
			KardExo.LOGGER.error(e);
		}
	}
	
	public void read()
	{
		if(this.file.exists())
		{
			KardExo.LOGGER.info("Reading {}", this.file.getName());
			
			try
			{
				FileReader reader = new FileReader(this.file);
				T data = GSON.fromJson(reader, this.typeToken.getType());
				this.setData(data);
			}
			catch(Exception e)
			{
				KardExo.LOGGER.error(e);
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
	
	private static class BoundingBoxTypeAdapter implements JsonDeserializer<BoundingBox>, JsonSerializer<BoundingBox>
	{
		@Override
		public BoundingBox deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject object = json.getAsJsonObject();
			int minX = object.get("minX").getAsInt();
			int minY = object.get("minY").getAsInt();
			int minZ = object.get("minZ").getAsInt();
			int maxX = object.get("maxX").getAsInt();
			int maxY = object.get("maxY").getAsInt();
			int maxZ = object.get("maxZ").getAsInt();
			return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		}
		
		@Override
		public JsonElement serialize(BoundingBox src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject json = new JsonObject();
			json.addProperty("minX", src.minX());
			json.addProperty("minY", src.minY());
			json.addProperty("minZ", src.minZ());
			json.addProperty("maxX", src.maxX());
			json.addProperty("maxY", src.maxY());
			json.addProperty("maxZ", src.maxZ());
			return json;
		}
	}
	
	public static class ResourceLocationTypeAdapter implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation>
	{
		public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			return ResourceLocation.parse(GsonHelper.convertToString(json, "location"));
		}
		
		public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context)
		{
			return new JsonPrimitive(src.toString());
		}
	}
	
	public static class ComponentSerializerAdapter implements JsonDeserializer<Component>, JsonSerializer<Component>
	{
		public Component deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
		{
			return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonParseException::new);
		}
		
		public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext)
		{
			return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow();
		}
	}
}
