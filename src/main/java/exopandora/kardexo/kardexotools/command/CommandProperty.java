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
import net.minecraft.command.arguments.ColumnPosArgument.ColumnPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
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
			source.sendFeedback(new TextComponentTranslation(indentation + (indentation.isEmpty() ? "Name" : "Child") + ": %s", property.getDisplayName()), false);
			
			String creators = property.getCreators(", ");
			
			if(!creators.isEmpty())
			{
				source.sendFeedback(new TextComponentString(indentation + " Creators: " + creators), false);
			}
			
			String owners = property.getOwners(", ");
			
			if(!owners.isEmpty())
			{
				source.sendFeedback(new TextComponentString(indentation + " Owners: " + owners), false);
			}
			
			source.sendFeedback(new TextComponentString(indentation + " X: [" + property.getXMin() + ", " + property.getXMax() + "]"), false);
			source.sendFeedback(new TextComponentString(indentation + " Z: [" + property.getZMin() + ", " + property.getZMax() + "]"), false);
			
			if(property.getChildren() != null)
			{
				count += list(source, property.getChildren(), " " + indentation);
			}
		}
		
		return count + list.size();
	}
	
	public static void add(String id, DimensionType dimension, ColumnPos from, ColumnPos to, String owner, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		double xMin = Math.min(from.field_212600_a, to.field_212600_a);
		double zMin = Math.min(from.field_212601_b, to.field_212601_b);
		
		double xMax = Math.max(from.field_212600_a, to.field_212600_a);
		double zMax = Math.max(from.field_212601_b, to.field_212601_b);
		
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
		double xMin = Math.min(from.field_212600_a, to.field_212600_a);
		double zMin = Math.min(from.field_212601_b, to.field_212601_b);
		
		double xMax = Math.max(from.field_212600_a, to.field_212600_a);
		double zMax = Math.max(from.field_212601_b, to.field_212601_b);
		
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
	
	public static void forOwner(String id, EntityPlayer player, DataFile<Property, String> file, Consumer<PropertyOwner> callback)
	{
		PropertyOwner owner = getOwner(id, player.getGameProfile().getName(), file);
		
		if(owner != null && callback != null)
		{
			callback.accept(owner);
		}
	}
	
	public static boolean hasPermission(CommandSource source, String id, EntityPlayer target, DataFile<Property, String> file)
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
