package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.util.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

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
		ServerLevel respawnDimension = source.getServer().findRespawnDimension();
		BlockPos blockPos = respawnDimension.getRespawnData().pos();
		Vec3 pos = blockPos.getCenter();
		int y = respawnDimension.getChunkAt(blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos.getX(), blockPos.getZ()) + 1;
		return CommandUtils.teleport(source, source.getPlayerOrException(), respawnDimension, BlockPos.containing(pos.x, y, pos.z));
	}
}
