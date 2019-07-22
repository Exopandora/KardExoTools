package exopandora.kardexo.kardexotools.command;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.command.arguments.BiomeArgument;
import exopandora.kardexo.kardexotools.config.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;

public class CommandLocateBiome
{
	private static final Map<String, Thread> LOCATORS = new HashMap<String, Thread>();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("locatebiome")
				.then(Commands.argument("biome", BiomeArgument.biome())
					.executes(context -> locate(context.getSource(), BiomeArgument.getBiome(context, "biome")))));
	}
	
	private static int locate(CommandSource source, Biome biome) throws CommandSyntaxException
	{
		if(LOCATORS.containsKey(source.getName()))
		{
			throw CommandBase.createException("Localization already in progress");
		}
		else
		{
			Thread thread = new Thread(() ->
			{
				ResourceLocation resource = Registry.BIOME.getKey(biome);
				BiomeProvider provider = source.getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider();
				
				boolean result = spiral(Config.LOCATE_BIOME_RADIUS, 16, new BlockPos(source.getPos()), (blockpos, x, z) -> blockpos.add(x, 0, z), blockpos ->
				{
					boolean contains = ArrayUtils.contains(provider.getBiomes(blockpos.getX(), blockpos.getZ(), 1, 1, false), biome);
					
					if(contains)
					{
						source.sendFeedback(new TranslationTextComponent("commands.locate.success", new Object[]{resource, blockpos.getX(), blockpos.getZ()}), false);
					}
					
					return contains;
				});
				
				if(!result)
				{
					source.sendFeedback(new TranslationTextComponent("commands.locate.failure", new Object[]{resource}), false);
				}
				
				LOCATORS.remove(source.getName());
			});
			
			LOCATORS.put(source.getName(), thread);
			thread.start();
			source.sendFeedback(new StringTextComponent("Searching..."), false);
		}
		
		return 1;
	}
	
	@FunctionalInterface
	public interface TriFunction<T, U, V, R>
	{
		R apply(T t, U u, V v);
	}
	
	private static <T> boolean spiral(int radius, int interval, T start, TriFunction<T, Integer, Integer, T> mapper, Function<T, Boolean> consumer)
	{
		int x = 0;
		int y = 0;
		
		if(consumer.apply(mapper.apply(start, x, y)))
		{
			return true;
		}
		
		if(radius > 30000000)
		{
			return false;
		}
		
		final int diameter = radius * 2;
		
		for(int delta = 1; delta <= diameter; delta++)
		{
			int direction = delta % 2 == 0 ? -1 : 1;
			
			for(int dx = 0; dx < delta; dx++)
			{
				x += direction;
				
				if(consumer.apply(mapper.apply(start, x * interval, y * interval)))
				{
					return true;
				}
			}
			
			for(int dy = 0; dy < delta; dy++)
			{
				y += direction;
				
				if(consumer.apply(mapper.apply(start, x * interval, y * interval)))
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
