package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public class CommandSpawn
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("spawn")
				.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSourceStack source) throws CommandSyntaxException
	{
		ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
		
		if(overworld != null)
		{
			return CommandBase.teleport(source, source.getPlayerOrException(), overworld, overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos()));
		}
		
		return 0;
	}
}
