package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;

public class CommandHome
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("home")
				.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSource source) throws CommandSyntaxException
	{
		ServerPlayerEntity sender = source.asPlayer();
		PlayerConfig config = Config.PLAYERS.getData().get(source.getName());
		
		if(config == null || sender.world == null || config != null && config.getHome() == null || config != null && config.getHome() != null && config.getHome().getPosition() == null)
		{
			throw CommandBase.exception("No home set");
		}
		
		MinecraftServer server = source.getServer();
		ServerWorld world = server.getWorld(RegistryKey.func_240903_a_(Registry.WORLD_KEY, config.getHome().getDimension()));
		BlockPos position = CommandHome.spawnPosition(world, config.getHome().getPosition());
		
		return CommandBase.teleport(source, sender, world, position);
	}
	
	private static BlockPos spawnPosition(ServerWorld world, BlockPos pos) throws CommandSyntaxException
	{
		BlockPos spawn = pos;
		
		while(!hasRoomForPlayer(world, spawn))
		{
			if(spawn.getY() > world.getHeight())
			{
				throw CommandBase.exception("Could not find safe position");
			}
			
			spawn = spawn.up();
		}
		
		return spawn;
	}
	
	protected static boolean hasRoomForPlayer(IBlockReader reader, BlockPos pos)
	{
		return Block.hasSolidSideOnTop(reader, pos.down()) && !reader.getBlockState(pos).getMaterial().isSolid() && !reader.getBlockState(pos.up()).getMaterial().isSolid();
	}
}
