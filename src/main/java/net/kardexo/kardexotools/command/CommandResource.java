package net.kardexo.kardexotools.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;

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
		
		for(BlockPos blockpos : BlockPos.Mutable.betweenClosed(area.x0, area.y0, area.z0, area.x1, area.y1, area.z1))
		{
			String location = source.getLevel().getBlockState(blockpos).getBlock().getDescriptionId();
			
			if(!location.equals(Blocks.AIR.getDescriptionId()))
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
			source.sendSuccess(new TranslationTextComponent("x" + entry.getValue() + " %s", new TranslationTextComponent(entry.getKey())), false);
		}
		
		return map.values().stream().reduce(Integer::sum).orElse(0);
	}
}
