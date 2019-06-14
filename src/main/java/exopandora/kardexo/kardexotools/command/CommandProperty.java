package exopandora.kardexo.kardexotools.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import exopandora.kardexo.kardexotools.base.Property;
import exopandora.kardexo.kardexotools.base.PropertyOwner;
import exopandora.kardexo.kardexotools.data.DataFile;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;

public abstract class CommandProperty
{
	public static int list(CommandSource source, DataFile<Property, String> file) throws NoSuchElementException
	{
		if(file.getData().values().isEmpty())
		{
			throw new NoSuchElementException();
		}
		
		return CommandProperty.list(source, file.getData().values(), "");
	}
	
	private static int list(CommandSource source, Collection<Property> list, String indentation)
	{
		int count = 0;
		
		for(Property property : list)
		{
			source.sendFeedback(new TranslationTextComponent(indentation + (indentation.isEmpty() ? "Name" : "Child") + ": %s", property.getDisplayName()), false);
			
			String creators = property.getCreators(", ");
			
			if(!creators.isEmpty())
			{
				source.sendFeedback(new StringTextComponent(indentation + " Creators: " + creators), false);
			}
			
			String owners = property.getOwners(", ");
			
			if(!owners.isEmpty())
			{
				source.sendFeedback(new StringTextComponent(indentation + " Owners: " + owners), false);
			}
			
			source.sendFeedback(new StringTextComponent(indentation + " X: [" + property.getXMin() + ", " + property.getXMax() + "]"), false);
			source.sendFeedback(new StringTextComponent(indentation + " Z: [" + property.getZMin() + ", " + property.getZMax() + "]"), false);
			
			if(property.getChildren() != null)
			{
				count += list(source, property.getChildren(), " " + indentation);
			}
		}
		
		return count + list.size();
	}
	
	public static void add(String id, DimensionType dimension, ColumnPos from, ColumnPos to, String owner, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		List<PropertyOwner> owners = new ArrayList<PropertyOwner>();
		owners.add(new PropertyOwner(owner, true, true, null, null));
		
		Property property = new Property(id, title, owners, dimension.getId(), xMin, zMin, xMax, zMax);
		
		if(file.getData().containsKey(property.getName()))
		{
			throw new IllegalStateException();
		}
		
		file.getData().put(property.getName(), property);
		file.save();
	}
	
	public static void remove(String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		if(file.getData().containsKey(id))
		{
			file.getData().remove(id);
			file.save();
		}
		
		throw new NoSuchElementException();
	}
	
	public static void addChild(Property parent, String id, DimensionType dimension, ColumnPos from, ColumnPos to, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		Property property = new Property(id, title, Collections.emptyList(), dimension.getId(), xMin, zMin, xMax, zMax);
		
		if(property.getChild(id) != null)
		{
			throw new IllegalStateException();
		}
		
		parent.addChild(property);
		file.save();
	}
	
	public static void removeChild(Property parent, String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		Property child = parent.getChild(id);
		
		if(child == null)
		{
			throw new NoSuchElementException();
		}
		
		parent.removeChild(child);
		file.save();
	}
	
	public static boolean isCreator(String name, String id, DataFile<Property, String> file)
	{
		return file.getData().get(id).isCreator(name);
	}
	
	public static boolean isOwner(String name, String id, DataFile<Property, String> file)
	{
		return file.getData().get(id).isOwner(name);
	}
	
	public static PropertyOwner getOwner(String id, String name, DataFile<Property, String> file)
	{
		for(PropertyOwner owner : file.getData().get(id).getAllOwners())
		{
			if(owner.getName().equals(name))
			{
				return owner;
			}
		}
		
		return null;
	}
	
	public static void forOwner(String id, PlayerEntity player, DataFile<Property, String> file, Consumer<PropertyOwner> callback)
	{
		PropertyOwner owner = getOwner(id, player.getGameProfile().getName(), file);
		
		if(owner != null && callback != null)
		{
			callback.accept(owner);
		}
	}
	
	public static boolean hasPermission(CommandSource source, String id, PlayerEntity target, DataFile<Property, String> file)
	{
		return source.hasPermissionLevel(4) || isCreator(source.getName(), id, file) || (target != null ? (isOwner(source.getName(), id, file) && target.equals(source.getEntity())) : false);
	}
	
	public static Property getProperty(String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		Property property = file.getData().get(id);
		
		if(property == null)
		{
			throw new NoSuchElementException();
		}
		
		return property;
	}
	
	public static CompletableFuture<Suggestions> getChildSuggestions(DataFile<Property, String> file, CommandContext<CommandSource> context, SuggestionsBuilder builder, String parent)
	{
		Property property = file.getData().get(parent);
		
		if(property != null)
		{
			return ISuggestionProvider.suggest(property.getChildrenNames(), builder);
		}
		
		return ISuggestionProvider.suggest(Collections.emptyList(), builder);
	}
}
