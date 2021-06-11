package net.kardexo.kardexotools.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.kardexo.kardexotools.config.DataFile;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class CommandProperty
{
	public static int list(CommandSourceStack source, Map<String, Property> data) throws NoSuchElementException
	{
		if(data.values().isEmpty())
		{
			throw new NoSuchElementException();
		}
		
		return CommandProperty.list(source, data.values(), "");
	}
	
	private static int list(CommandSourceStack source, Collection<Property> list, String indentation)
	{
		int count = 0;
		
		for(Property property : list)
		{
			source.sendSuccess(new TranslatableComponent(indentation + (indentation.isEmpty() ? "Name" : "Child") + ": %s", property.getDisplayName()), false);
			
			String creators = property.getCreators(", ");
			
			if(!creators.isEmpty())
			{
				source.sendSuccess(new TextComponent(indentation + " Creators: " + creators), false);
			}
			
			String owners = property.getOwners(", ");
			
			if(!owners.isEmpty())
			{
				source.sendSuccess(new TextComponent(indentation + " Owners: " + owners), false);
			}
			
			source.sendSuccess(new TextComponent(indentation + " X: [" + property.getXMin() + ", " + property.getXMax() + "]"), false);
			source.sendSuccess(new TextComponent(indentation + " Z: [" + property.getZMin() + ", " + property.getZMax() + "]"), false);
			
			if(property.getChildren() != null)
			{
				count += list(source, property.getChildren(), " " + indentation);
			}
		}
		
		return count + list.size();
	}
	
	public static CompletableFuture<Suggestions> getChildSuggestions(DataFile<Property, String> file, CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, String parent)
	{
		Property property = file.get(parent);
		
		if(property != null)
		{
			return SharedSuggestionProvider.suggest(property.getChildrenNames(), builder);
		}
		
		return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
	}
}
