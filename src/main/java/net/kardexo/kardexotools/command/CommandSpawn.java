package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class CommandSpawn
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("spawn")
				.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSource source) throws CommandSyntaxException
	{
		ServerWorld overworld = source.getServer().getWorld(World.OVERWORLD);
		
		if(overworld != null)
		{
			return CommandBase.teleport(source, source.asPlayer(), overworld, overworld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, overworld.getSpawnPoint()));
		}
		
		return 0;
	}
}
