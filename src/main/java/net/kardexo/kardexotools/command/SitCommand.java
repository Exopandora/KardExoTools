package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.mixinducks.ISittingCapableEntity;
import net.kardexo.kardexotools.util.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SitCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("sit")
			.requires(source -> KardExo.CONFIG.getData().isSitCommandEnabled())
				.executes(context -> SitCommand.toggleSitting(context.getSource()))
			.then(Commands.literal("on")
				.executes(context -> SitCommand.setSittingEnabled(context.getSource(), true)))
			.then(Commands.literal("off")
				.executes(context -> SitCommand.setSittingEnabled(context.getSource(), false))));
	}
	
	private static int toggleSitting(CommandSourceStack source) throws CommandSyntaxException
	{
		ISittingCapableEntity player = (ISittingCapableEntity) source.getPlayerOrException();
		
		if(player.kardexotools$isSitting())
		{
			player.kardexotools$stopSitting();
			return 0;
		}
		else
		{
			player.kardexotools$startSitting();
			return 1;
		}
	}
	
	private static int setSittingEnabled(CommandSourceStack source, boolean enabled)
	{
		KardExo.PLAYERS.getData().computeIfAbsent(CommandUtils.getUUID(source), key -> new PlayerConfig()).setSittingEnabled(enabled);
		KardExo.PLAYERS.save();
		
		if(enabled)
		{
			source.sendSuccess(() -> Component.literal("Sitting enabled"), false);
		}
		else
		{
			source.sendSuccess(() -> Component.literal("Sitting disabled"), false);
		}
		
		return 1;
	}
}
