package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StacksCommand
{
	private static final int STORAGE_BOX_ITEMS = 27 * 64;
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("stacks")
				.then(Commands.argument("count", IntegerArgumentType.integer(1))
						.executes(context -> stacks(context.getSource(), IntegerArgumentType.getInteger(context, "count")))));
	}
	
	private static int stacks(CommandSourceStack source, int count) throws CommandSyntaxException
	{
		int boxes = count / STORAGE_BOX_ITEMS;
		int stacks = (count - boxes * STORAGE_BOX_ITEMS) / 64;
		int items = count % 64;
		
		if(boxes > 0)
		{
			source.sendSuccess(Component.literal("Storage Boxes: " + boxes), false);
		}
		
		if(stacks > 0)
		{
			source.sendSuccess(Component.literal("Stacks: " + stacks), false);
		}
		
		source.sendSuccess(Component.literal("Items: " + items), false);
		return count;
	}
}
