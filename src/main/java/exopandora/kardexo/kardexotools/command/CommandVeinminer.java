package exopandora.kardexo.kardexotools.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.config.Config;
import exopandora.kardexo.kardexotools.config.PlayerConfig;
import exopandora.kardexo.kardexotools.veinminer.VeinminerConfigEntry;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandVeinminer
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("veinminer")
				.then(Commands.literal("on")
					.executes(context -> CommandVeinminer.setVeinminer(context.getSource(), true)))
				.then(Commands.literal("off")
					.executes(context -> CommandVeinminer.setVeinminer(context.getSource(), false)))
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource()))));
	}
	
	private static int setVeinminer(CommandSource source, boolean enabled) throws CommandSyntaxException
	{
		Config.PLAYERS.getData().put(source.getName(), new PlayerConfig(source.getName(), enabled));
		
		if(enabled)
		{
			source.sendFeedback(new StringTextComponent("Veinminer enabled"), false);
		}
		else
		{
			source.sendFeedback(new StringTextComponent("Veinminer disabled"), false);
		}
		
		return 1;
	}
	
	private static int list(CommandSource source) throws CommandSyntaxException
	{
		List<ITextComponent> list = new ArrayList<ITextComponent>(Config.VEINMINER.getData().size());
		
		for(Entry<Block, VeinminerConfigEntry> entry : Config.VEINMINER.getData().entrySet())
		{
			ItemStack stack = new ItemStack(entry.getKey().asItem(), 1);
			list.add(new TranslationTextComponent("%s = %s", stack.getDisplayName(), entry.getValue().getRadius()));
		}
		
		list.sort((a, b) -> a.toString().compareTo(b.toString()));
		list.forEach(message -> source.sendFeedback(message, false));
		
		return list.size();
	}
}
