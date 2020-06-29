package net.kardexo.kardexotools.config;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.util.math.BlockPos;

public class BlockPosTypeAdapter implements JsonDeserializer<BlockPos>, JsonSerializer<BlockPos>
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

