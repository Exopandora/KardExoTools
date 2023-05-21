package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.util.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("spawn")
			.requires(source -> KardExo.CONFIG.getData().isSpawnCommandEnabled())
			.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSourceStack source) throws CommandSyntaxException
	{
		ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
		
		if(overworld != null)
		{
			return CommandUtils.teleport(source, source.getPlayerOrException(), overworld, overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos()));
		}
		
		return 0;
	}
}
