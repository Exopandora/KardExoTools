package net.kardexo.kardexotools.command;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.kardexo.kardexotools.config.MapFile;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;

public abstract class PropertyCommandUtils
{
	public static int list(CommandSourceStack source, Map<String, Property> properties) throws NoSuchElementException
	{
		if(properties == null || properties.isEmpty())
		{
			throw new NoSuchElementException();
		}
		
		source.sendSuccess(ComponentUtils.formatList(properties.entrySet(), entry -> entry.getValue().getDisplayName(entry.getKey(), source.getServer().getProfileCache())), false);
		return properties.size();
	}
	
	public static CompletableFuture<Suggestions> getChildSuggestions(MapFile<String, Property> file, CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, String parent)
	{
		Property property = file.get(parent);
		
		if(property == null)
		{
			return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
		}
		
		return SharedSuggestionProvider.suggest(property.getChildrenIds(), builder);
	}
}
