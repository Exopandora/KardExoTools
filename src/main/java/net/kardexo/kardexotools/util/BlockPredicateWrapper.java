package net.kardexo.kardexotools.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.kardexotools.mixin.AccessorBlockPredicate;
import net.kardexo.kardexotools.mixin.AccessorTagPredicate;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.lang.reflect.Type;

public class BlockPredicateWrapper
{
	private final BlockPredicateArgument.Result result;
	
	public BlockPredicateWrapper(BlockPredicateArgument.Result result)
	{
		this.result = result;
	}
	
	public boolean matches(Level level, BlockPos pos)
	{
		return this.result.test(new BlockInWorld(level, pos, false));
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		if(this.result instanceof BlockPredicateArgument.BlockPredicate)
		{
			var accessor = (AccessorBlockPredicate) this.result;
			builder.append(BuiltInRegistries.BLOCK.getKey(accessor.getState().getBlock()));
			var properties = accessor.getProperties();
			
			if(!properties.isEmpty())
			{
				builder.append('[');
				
				for(var property : properties)
				{
					builder.append(property.getName());
					builder.append("=");
					builder.append(accessor.getState().getValue(property));
				}
				
				builder.append(']');
			}
			
			if(accessor.getNbt() != null)
			{
				builder.append(accessor.getNbt().toString());
			}
		}
		else if(this.result instanceof BlockPredicateArgument.TagPredicate)
		{
			var accessor = (AccessorTagPredicate) this.result;
			builder.append('#');
			builder.append(((HolderSet.Named<?>) accessor.getTag()).key().location());
			var properties = accessor.getVagueProperties();
			
			if(!properties.isEmpty())
			{
				builder.append('[');
				
				for(var entry : properties.entrySet())
				{
					builder.append(entry.getKey());
					builder.append("=");
					builder.append(entry.getValue());
				}
				
				builder.append(']');
			}
			
			if(accessor.getNbt() != null)
			{
				builder.append(accessor.getNbt().toString());
			}
		}
		
		return builder.toString();
	}
	
	public static class Serializer implements JsonDeserializer<BlockPredicateWrapper>, JsonSerializer<BlockPredicateWrapper>
	{
		@Override
		public BlockPredicateWrapper deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
		{
			try
			{
				return new BlockPredicateWrapper(BlockPredicateArgument.parse(BuiltInRegistries.BLOCK, new StringReader(element.getAsString())));
			}
			catch(CommandSyntaxException e)
			{
				throw new JsonParseException(e.getMessage(), e);
			}
		}
		
		@Override
		public JsonElement serialize(BlockPredicateWrapper predicate, Type type, JsonSerializationContext context)
		{
			return new JsonPrimitive(predicate.toString());
		}
	}
}
