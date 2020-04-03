package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.base.Home;
import exopandora.kardexo.kardexotools.config.Config;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.dimension.DimensionType;
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
		Home home = Config.HOME.getData().get(source.getName());
		
		if(home == null || sender.world == null)
		{
			throw CommandBase.exception("No home set");
		}
		
		MinecraftServer server = source.getServer();
		ServerWorld world = server.getWorld(DimensionType.getById(home.getDimension()));
		BlockPos position = CommandHome.spawnPosition(server.getWorld(home.getDimensionType()), home.getPosition());
		
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
