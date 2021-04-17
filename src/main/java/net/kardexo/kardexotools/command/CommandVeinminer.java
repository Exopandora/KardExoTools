package net.kardexo.kardexotools.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.VeinBlockConfig;
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
		KardExo.PLAYERS.computeIfAbsent(source.getTextName(), PlayerConfig::new).setVeinminerEnabled(enabled);
		KardExo.PLAYERS_FILE.save();
		
		if(enabled)
		{
			source.sendSuccess(new StringTextComponent("Veinminer enabled"), false);
		}
		else
		{
			source.sendSuccess(new StringTextComponent("Veinminer disabled"), false);
		}
		
		return 1;
	}
	
	private static int list(CommandSource source) throws CommandSyntaxException
	{
		List<ITextComponent> list = new ArrayList<ITextComponent>(KardExo.VEINMINER.size());
		
		for(Entry<Block, VeinBlockConfig> entry : KardExo.VEINMINER.entrySet())
		{
			ItemStack stack = new ItemStack(entry.getKey().asItem(), 1);
			list.add(new TranslationTextComponent("%s = %s", stack.getDisplayName(), entry.getValue().getRadius()));
		}
		
		list.sort((a, b) -> a.toString().compareTo(b.toString()));
		list.forEach(message -> source.sendSuccess(message, false));
		
		return list.size();
	}
}
