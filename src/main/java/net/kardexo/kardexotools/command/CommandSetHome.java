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
		ServerPlayerEntity sender = source.getPlayerOrException();
		BlockPos pos = sender.blockPosition();
		
		Config.PLAYERS.getData().computeIfAbsent(source.getTextName(), PlayerConfig::new).setHome(new PlayerHome(pos, sender.level.dimension().location()));
		Config.save(Config.PLAYERS);
		
		source.sendSuccess(new StringTextComponent("Home set to " + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
		return 1;
	}
}
