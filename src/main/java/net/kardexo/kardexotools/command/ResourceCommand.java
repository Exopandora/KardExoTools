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
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
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
		Map<Block, Integer> map = new HashMap<Block, Integer>();
		
		for(BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ()))
		{
			Block block = source.getLevel().getBlockState(blockpos).getBlock();
			
			if(!Blocks.AIR.equals(block))
			{
				map.compute(block, (key, value) -> value == null ? 1 : value + 1);
			}
		}
		
		for(Entry<Block, Integer> entry : map.entrySet())
		{
			source.sendSuccess(Component.translatable("x" + entry.getValue() + " %s", Component.translatable(entry.getKey().getDescriptionId())), false);
		}
		
		return map.values().stream().reduce(Integer::sum).orElse(0);
	}
}
