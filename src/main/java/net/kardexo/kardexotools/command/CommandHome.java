package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

public class CommandHome
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("home")
				.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSourceStack source) throws CommandSyntaxException
	{
		ServerPlayer sender = source.getPlayerOrException();
		PlayerConfig config = KardExo.PLAYERS.get(source.getTextName());
		
		if(config == null || sender.level == null || config != null && config.getHome() == null || config != null && config.getHome() != null && config.getHome().getPosition() == null)
		{
			throw CommandBase.exception("No home set");
		}
		
		MinecraftServer server = source.getServer();
		ServerLevel level = server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, config.getHome().getDimension()));
		BlockPos position = CommandHome.spawnPosition(level, config.getHome().getPosition());
		
		return CommandBase.teleport(source, sender, level, position);
	}
	
	private static BlockPos spawnPosition(ServerLevel level, BlockPos pos) throws CommandSyntaxException
	{
		BlockPos spawn = pos;
		
		while(!hasRoomForPlayer(level, spawn))
		{
			if(spawn.getY() > level.getHeight())
			{
				throw CommandBase.exception("Could not find safe position");
			}
			
			spawn = spawn.above();
		}
		
		return spawn;
	}
	
	protected static boolean hasRoomForPlayer(BlockGetter getter, BlockPos pos)
	{
		return Block.canSupportRigidBlock(getter, pos.below()) && !getter.getBlockState(pos).getMaterial().isSolid() && !getter.getBlockState(pos.above()).getMaterial().isSolid();
	}
}
