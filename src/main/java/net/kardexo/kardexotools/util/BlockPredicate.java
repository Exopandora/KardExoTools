package net.kardexo.kardexotools.util;

import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicate
{
	private ResourceLocation block;
	@Nullable
	private Map<String, String> properties;
	@Nullable
	private CompoundTag nbt;
	private boolean isTag;
	
	public BlockPredicate(ResourceLocation block, @Nullable Map<String, String> properties, @Nullable CompoundTag nbt, boolean isTag)
	{
		this.block = block;
		this.properties = properties;
		this.nbt = nbt;
		this.isTag = isTag;
	}
	
	public boolean matches(Level level, BlockPos pos)
	{
		BlockState blockState = level.getBlockState(pos);
		
		if(this.isTag)
		{
			TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, this.block);
			
			if(!blockState.is(tag))
			{
				return false;
			}
			
			if(this.properties != null)
			{
				for(Entry<String, String> entry : this.properties.entrySet())
				{
					Property<?> property = blockState.getBlock().getStateDefinition().getProperty(entry.getKey());
					
					if(property == null)
					{
						return false;
					}
					
					Comparable<?> comparable = property.getValue(entry.getValue()).orElse(null);
					
					if(comparable == null || blockState.getValue(property) != comparable)
					{
						return false;
					}
				}
			}
			
			if(this.nbt != null)
			{
				BlockEntity blockEntity = level.getBlockEntity(pos);
				return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.saveWithFullMetadata(), true);
			}
			
			return true;
		}
		
		Block block = Registry.BLOCK.get(this.block);
		
		if(!blockState.is(block))
		{
			return false;
		}
		
		if(this.properties != null)
		{
			Map<String, String> properties = propertiesToString(blockState.getValues());
			
			for(Entry<String, String> property : this.properties.entrySet())
			{
				if(!property.getValue().equals(properties.get(property.getKey())))
				{
					return false;
				}
			}
		}
		
		if(this.nbt != null)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			return (blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.saveWithFullMetadata(), true));
		}
		
		return true;
	}
	
	public ResourceLocation getBlock()
	{
		return this.block;
	}
	
	@NotNull
	public Map<String, String> getProperties()
	{
		if(this.properties == null)
		{
			return Collections.emptyMap();
		}
		
		return Collections.unmodifiableMap(this.properties);
	}
	
	@Nullable
	public CompoundTag getNbt()
	{
		return this.nbt;
	}
	
	public boolean isTag()
	{
		return this.isTag;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		if(this.isTag())
		{
			builder.append('#');
		}
		
		builder.append(this.block.toString());
		
		if(this.properties != null && !this.properties.isEmpty())
		{
			builder.append('[');
			builder.append(this.properties.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(",")));
			builder.append(']');
		}
		
		if(this.nbt != null)
		{
			builder.append(this.nbt.toString());
		}
		
		return builder.toString();
	}
	
	public static class Serializer implements JsonDeserializer<BlockPredicate>, JsonSerializer<BlockPredicate> 
	{
		@Override
		public BlockPredicate deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
		{
			try
			{
				return SimpleBlockPredicateParser.parse(element.getAsString());
			}
			catch(CommandSyntaxException e)
			{
				throw new JsonParseException(e.getMessage());
			}
		}
		
		@Override
		public JsonElement serialize(BlockPredicate predicate, Type type, JsonSerializationContext context)
		{
			return new JsonPrimitive(predicate.toString());
		}
	}
	
	private static Map<String, String> propertiesToString(Map<Property<?>, Comparable<?>> properties)
	{
		return properties.entrySet().stream().map(entry ->
		{
	        Property<?> property = entry.getKey();
	        return new SimpleEntry<String, String>(property.getName(), getName(property, entry.getValue()));
		}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
    @SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String getName(Property<T> key, Comparable<?> value)
    {
       return key.getName((T) value);
    }
}
