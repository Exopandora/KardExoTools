package net.kardexo.kardexotools.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.VeinBlockConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CommandVeinminer
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("veinminer")
				.then(Commands.literal("on")
					.executes(context -> CommandVeinminer.setVeinminer(context.getSource(), true)))
				.then(Commands.literal("off")
					.executes(context -> CommandVeinminer.setVeinminer(context.getSource(), false)))
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource()))));
	}
	
	private static int setVeinminer(CommandSourceStack source, boolean enabled) throws CommandSyntaxException
	{
		KardExo.PLAYERS.computeIfAbsent(source.getTextName(), PlayerConfig::new).setVeinminerEnabled(enabled);
		KardExo.PLAYERS_FILE.save();
		
		if(enabled)
		{
			source.sendSuccess(new TextComponent("Veinminer enabled"), false);
		}
		else
		{
			source.sendSuccess(new TextComponent("Veinminer disabled"), false);
		}
		
		return 1;
	}
	
	private static int list(CommandSourceStack source) throws CommandSyntaxException
	{
		List<Component> list = new ArrayList<Component>(KardExo.VEINMINER.size());
		
		for(Entry<Block, VeinBlockConfig> entry : KardExo.VEINMINER.entrySet())
		{
			ItemStack stack = new ItemStack(entry.getKey().asItem(), 1);
			list.add(new TranslatableComponent("%s = %s", stack.getDisplayName(), entry.getValue().getRadius()));
		}
		
		list.sort((a, b) -> a.toString().compareTo(b.toString()));
		list.forEach(message -> source.sendSuccess(message, false));
		
		return list.size();
	}
}
