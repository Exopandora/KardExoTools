package net.kardexo.kardexotools.command;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.kardexo.kardexotools.config.DataFile;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
