package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.PlayerHome;
import net.kardexo.kardexotools.util.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SetHomeCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("sethome")
				.executes(context -> setHome(context.getSource())));
	}
	
	private static int setHome(CommandSourceStack source) throws CommandSyntaxException
	{
		ServerPlayer sender = source.getPlayerOrException();
		BlockPos pos = sender.blockPosition();
		
		KardExo.PLAYERS.computeIfAbsent(CommandUtils.getUUID(source), key -> new PlayerConfig()).setHome(new PlayerHome(pos, sender.level.dimension().location()));
		KardExo.PLAYERS_FILE.save();
		
		source.sendSuccess(new TextComponent("Home set to " + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
		return 1;
	}
}
