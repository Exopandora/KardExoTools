package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.VeinConfig;
import net.kardexo.kardexotools.util.BlockPredicateWrapper;
import net.kardexo.kardexotools.util.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class VeinminerCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("veinminer")
			.requires(source -> KardExo.CONFIG.getData().isVeinminerCommandEnabled())
			.then(Commands.literal("on")
				.executes(context -> VeinminerCommand.setVeinminer(context.getSource(), true)))
			.then(Commands.literal("off")
				.executes(context -> VeinminerCommand.setVeinminer(context.getSource(), false)))
			.then(Commands.literal("list")
				.executes(context -> list(context.getSource()))));
	}
	
	private static int setVeinminer(CommandSourceStack source, boolean enabled) throws CommandSyntaxException
	{
		KardExo.PLAYERS.getData().computeIfAbsent(CommandUtils.getUUID(source), key -> new PlayerConfig()).setVeinminerEnabled(enabled);
		KardExo.PLAYERS.save();
		
		if(enabled)
		{
			source.sendSuccess(() -> Component.literal("Veinminer enabled"), false);
		}
		else
		{
			source.sendSuccess(() -> Component.literal("Veinminer disabled"), false);
		}
		
		return 1;
	}
	
	private static int list(CommandSourceStack source) throws CommandSyntaxException
	{
		List<Component> list = new ArrayList<Component>(KardExo.VEINMINER.getData().size());
		
		for(Entry<BlockPredicateWrapper, VeinConfig> config : KardExo.VEINMINER.getData().entrySet())
		{
			list.add(Component.translatable("%s = %s", config.getKey().toString(), config.getValue().getRadius()));
		}
		
		list.sort(Comparator.comparing(Object::toString));
		list.forEach(message -> source.sendSuccess(() -> message, false));
		
		return list.size();
	}
}
