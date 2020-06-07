package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.PlayerHome;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class CommandSetHome
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("sethome")
				.executes(context -> setHome(context.getSource())));
	}
	
	private static int setHome(CommandSource source) throws CommandSyntaxException
	{
		ServerPlayerEntity sender = source.asPlayer();
		BlockPos pos = sender.getPosition();
		
		Config.PLAYERS.getData().computeIfAbsent(source.getName(), PlayerConfig::new).setHome(new PlayerHome(pos, sender.dimension.getId()));
		Config.PLAYERS.save();
		
		source.sendFeedback(new StringTextComponent("Home set to " + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
		return 1;
	}
}
