package exopandora.kardexo.kardexotools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

public class CommandLocateBiome extends CommandBase
{
	private static final Map<String, Thread> LOCALIZATION = new HashMap<String, Thread>();
	
	@Override
	public String getName()
	{
		return "locatebiome";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/locatebiome <biome>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			ResourceLocation resource = new ResourceLocation(args[0]);
			
			if(Biome.REGISTRY.containsKey(resource))
			{
				if(LOCALIZATION.containsKey(sender.getName()))
				{
					throw new CommandException("Localization already in progress");
				}
				else
				{
					Thread thread = new Thread(() ->
					{
						Biome biome = Biome.REGISTRY.getObject(resource);
						BiomeProvider provider = sender.getEntityWorld().getBiomeProvider();
						
						boolean result = spiral(Config.LOCATE_BIOME_RADIUS, 16, sender.getPosition(), (blockpos, x, z) -> blockpos.add(x, 0, z), blockpos ->
						{
							boolean contains = ArrayUtils.contains(provider.getBiomes(null, blockpos.getX(), blockpos.getZ(), 1, 1, false), biome);
							
							if(contains)
							{
								sender.sendMessage(new TextComponentTranslation("commands.locate.success", new Object[]{resource, blockpos.getX(), blockpos.getZ()}));
							}
							
							return contains;
						});
						
						if(!result)
						{
							sender.sendMessage(new TextComponentTranslation("commands.locate.failure", new Object[]{resource}));
						}
						
						LOCALIZATION.remove(sender.getName());
					});
					
					LOCALIZATION.put(sender.getName(), thread);
					thread.start();
					sender.sendMessage(new TextComponentString("Searching..."));
				}
			}
			else
			{
				throw new CommandException("Invalid biome");
			}
		}
		else
		{
			throw new WrongUsageException(this.getUsage(sender));
		}
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? this.getListOfStringsMatchingLastWord(args, Biome.REGISTRY.getKeys()) : Collections.<String>emptyList();
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
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
