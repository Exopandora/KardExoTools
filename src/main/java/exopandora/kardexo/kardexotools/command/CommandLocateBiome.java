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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;

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
			throw CommandBase.exception("Search already in progress");
		}
		else
		{
			Thread thread = new Thread(() ->
			{
				BlockPos start = new BlockPos(source.getPos());
				ResourceLocation resource = Registry.BIOME.getKey(biome);
				BlockPos result = CommandLocateBiome.spiral(Config.LOCATE_BIOME_RADIUS, 16, start, (blockpos, x, z) -> blockpos.add(x, 0, z), blockpos ->
				{
					BiomeContainer biomeContainer = source.getWorld().getChunk(blockpos).func_225549_i_();
					
					if(biomeContainer != null && ArrayUtils.contains(biomeContainer.getBiomeArray(), biome))
					{
						return blockpos;
					}
					
					return null;
				});
				
				if(result == null)
				{
					source.sendFeedback(new TranslationTextComponent("commands.locate.failure", resource), false);
				}
				else
				{
					int distance = MathHelper.floor(CommandLocateBiome.distance(start.getX(), start.getZ(), result.getX(), result.getZ()));
					Style style = new Style().setColor(TextFormatting.GREEN)
			                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + result.getX() + " ~ " + result.getZ()))
			                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")));
		            ITextComponent position = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", result.getX(), "~", result.getZ())).setStyle(style);
					source.sendFeedback(new TranslationTextComponent("commands.locate.success", resource, position, distance), false);
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
	
	private static <T> T spiral(int radius, int interval, T start, TriFunction<T, Integer, Integer, T> mapper, Function<T, T> consumer)
	{
		int x = 0;
		int y = 0;
		
		T result = consumer.apply(mapper.apply(start, x, y));
		
		if(result != null)
		{
			return result;
		}
		
		if(radius > 30000000)
		{
			return null;
		}
		
		final int diameter = radius * 2;
		
		for(int delta = 1; delta <= diameter; delta++)
		{
			int direction = delta % 2 == 0 ? -1 : 1;
			
			for(int dx = 0; dx < delta; dx++)
			{
				x += direction;
				result = consumer.apply(mapper.apply(start, x * interval, y * interval));
				
				if(result != null)
				{
					return result;
				}
			}
			
			for(int dy = 0; dy < delta; dy++)
			{
				y += direction;
				result = consumer.apply(mapper.apply(start, x * interval, y * interval));
				
				if(result != null)
				{
					return result;
				}
			}
		}
		
		return null;
	}
	
    private static float distance(int minX, int minZ, int maxX, int maxZ)
    {
        int dx = maxX - minX;
        int dy = maxZ - minZ;
        
        return MathHelper.sqrt((float)(dx * dx + dy * dy));
    }
}
