package net.kardexo.kardexotools.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ResourceCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("resource")
				.then(Commands.argument("from", BlockPosArgument.blockPos())
					.then(Commands.argument("to", BlockPosArgument.blockPos())
						.executes(context -> resource(context.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")))))));
	}
	
	private static int resource(CommandSourceStack source, BoundingBox area) throws CommandSyntaxException
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		for(BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ()))
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
			source.sendSuccess(new TranslatableComponent("x" + entry.getValue() + " %s", new TranslatableComponent(entry.getKey())), false);
		}
		
		return map.values().stream().reduce(Integer::sum).orElse(0);
	}
}
