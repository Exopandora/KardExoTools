package exopandora.kardexo.kardexotools;

import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.BiomeProvider;

public class CommandLocateBiome extends CommandBase
{
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
		sender.sendMessage(new TextComponentString("Currently not supported"));
		
//		BiomeProvider provider = sender.getEntityWorld().getBiomeProvider();
//		
//		int startX = sender.getPosition().getX();
//		int startZ = sender.getPosition().getZ();
//		
//		boolean result = spiral(10, 16, sender.getPosition(), (pos, x, z) -> pos.add(x, 0, z), pos ->
//		{
//			boolean contains = ArrayUtils.contains(provider.getBiomes(null, pos.getX(), pos.getZ(), 1, 1, false), Biomes.DESERT);
//			
//			if(contains)
//			{
//				sender.sendMessage(new TextComponentString(pos.toString()));
//			}
//			
//			return contains;
//		});
//		
//		if(!result)
//		{
//			sender.sendMessage(new TextComponentString("No result"));
//		}
	}
	
	public interface TriFunction<T, U, V, R>
	{
		R apply(T t, U u, V v);
	}
	
	private static <T> boolean spiral(int max, int interval, T start, TriFunction<T, Integer, Integer, T> mapper, Function<T, Boolean> consumer)
	{
		int x = 0;
		int y = 0;
		
		if(consumer.apply(mapper.apply(start, x, y)))
		{
			return true;
		}
		
		for(int delta = 1; delta <= max; delta++)
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
