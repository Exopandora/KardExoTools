package exopandora.kardexo.kardexotools.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandResource
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("resource")
				.then(Commands.argument("from", BlockPosArgument.blockPos())
					.then(Commands.argument("to", BlockPosArgument.blockPos())
						.executes(context -> resource(context.getSource(), new MutableBoundingBox(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")))))));
	}
	
	private static int resource(CommandSource source, MutableBoundingBox area) throws CommandSyntaxException
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		
        for(BlockPos blockpos : BlockPos.MutableBlockPos.getAllInBox(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ))
        {
        	String location = source.getWorld().getBlockState(blockpos).getBlock().getTranslationKey();
			
			if(!location.equals(Blocks.AIR.getTranslationKey()))
			{
				if(!map.containsKey(location))
				{
					map.put(location, 1);
				}
				else
				{
					map.put(location, map.get(location) + 1);
				}
			}
        }
		
		for(Entry<String, Integer> entry : map.entrySet())
		{
			source.sendFeedback(new TextComponentTranslation("x" + entry.getValue() + " %s", new TextComponentTranslation(entry.getKey())), false);
		}
		
		return map.values().stream().reduce(Integer::sum).orElse(0);
	}
}
