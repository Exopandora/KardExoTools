package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PackCommand
{
	private static final int STORAGE_BOX_STACKS = 27;
	private static final int MAX_STACK_SIZE = 64;
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("pack")
			.requires(source -> KardExo.CONFIG.getData().isPackCommandEnabled())
			.then(Commands.argument("count", IntegerArgumentType.integer(1))
				.executes(context -> pack(context.getSource(), IntegerArgumentType.getInteger(context, "count"), 64))
				.then(Commands.argument("stackSize", IntegerArgumentType.integer(1, MAX_STACK_SIZE))
					.executes(context -> pack(context.getSource(), IntegerArgumentType.getInteger(context, "count"), IntegerArgumentType.getInteger(context, "stackSize"))))));
	}
	
	private static int pack(CommandSourceStack source, int count, int stackSize) throws CommandSyntaxException
	{
		int storageBoxItems = STORAGE_BOX_STACKS * stackSize;
		int boxes = count / storageBoxItems;
		int stacks = (count - boxes * storageBoxItems) / stackSize;
		int items = count % stackSize;
		
		source.sendSuccess(() -> Component.literal(count + " items with a stack size of " + stackSize + " pack into:"), false);
		
		if(boxes > 0)
		{
			source.sendSuccess(() -> Component.literal("  " + boxes + " storage boxes"), false);
		}
		
		if(stacks > 0)
		{
			source.sendSuccess(() -> Component.literal("  " + stacks + " stacks"), false);
		}
		
		if(items > 0)
		{
			source.sendSuccess(() -> Component.literal("  " + items + " items"), false);
		}
		
		return count;
	}
}
