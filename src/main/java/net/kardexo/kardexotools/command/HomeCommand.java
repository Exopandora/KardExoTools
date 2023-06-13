package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.util.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HomeCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("home")
			.requires(source -> KardExo.CONFIG.getData().isHomeCommandEnabled())
				.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSourceStack source) throws CommandSyntaxException
	{
		ServerPlayer sender = source.getPlayerOrException();
		PlayerConfig config = KardExo.PLAYERS.get(CommandUtils.getUUID(source));
		
		if(config == null || sender.level() == null || config != null && config.getHome() == null || config != null && config.getHome() != null && config.getHome().getPosition() == null)
		{
			throw CommandUtils.simpleException("No home set");
		}
		
		MinecraftServer server = source.getServer();
		ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, config.getHome().getDimension()));
		BlockPos position = spawnPosition(level, config.getHome().getPosition(), sender);
		return CommandUtils.teleport(source, sender, level, position);
	}
	
	private static BlockPos spawnPosition(ServerLevel serverLevel, BlockPos pos, Entity entity) throws CommandSyntaxException
	{
		for(BlockPos mutableBlockPos = pos.below(); pos.getY() <= serverLevel.getMaxBuildHeight(); mutableBlockPos = mutableBlockPos.above())
		{
			BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
			
			if(!blockState.getFluidState().isEmpty())
			{
				break;
			}
			
			if(Block.isFaceFull(blockState.getCollisionShape(serverLevel, mutableBlockPos), Direction.UP))
			{
				BlockPos spawn = mutableBlockPos.above();
				
				if(DismountHelper.findSafeDismountLocation(entity.getType(), serverLevel, spawn, true) != null)
				{
					return spawn;
				}
			}
		}
		
		throw CommandUtils.simpleException("Could not find safe position");
	}
}
