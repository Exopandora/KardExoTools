package exopandora.kardexo.kardexotools.command.arguments;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;

public class BiomeArgument implements ArgumentType<Biome>
{
	public static final DynamicCommandExceptionType BIOME_NOT_FOUND = new DynamicCommandExceptionType(biome -> new TextComponentTranslation("Unknown biome %s", new Object[]{biome}));
	
	public static BiomeArgument biome()
	{
		return new BiomeArgument();
	}
	
	public static Biome getBiome(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, Biome.class);
	}
	
	@Override
	public Biome parse(StringReader reader) throws CommandSyntaxException
	{
		ResourceLocation resourcelocation = ResourceLocation.read(reader);
		Biome biome = IRegistry.BIOME.get(resourcelocation);
		
		if(biome == null)
		{
			throw BIOME_NOT_FOUND.create(resourcelocation);
		}
		
		return biome;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggestIterable(IRegistry.BIOME.keySet(), builder);
	}
}
