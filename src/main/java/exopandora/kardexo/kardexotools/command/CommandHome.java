package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.base.Home;
import exopandora.kardexo.kardexotools.data.Config;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;

public class CommandHome
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("home")
				.executes(context -> execute(context.getSource())));
	}
	
	private static int execute(CommandSource source) throws CommandSyntaxException
	{
		Entity sender = source.assertIsEntity();
		Home home = Config.HOME.getData().get(source.getName());
		
		if(home == null || sender.world == null)
		{
			throw CommandBase.createException("No home set");
		}
		
		MinecraftServer server = source.getServer();
		CommandHome.doTeleport(server, sender, getPlayerSpawnPosition(server.getWorld(home.getDimensionType()), home.getPosition()), home.getDimension());
		
		return 1;
	}
	
	private static BlockPos getPlayerSpawnPosition(ServerWorld world, BlockPos pos) throws CommandSyntaxException
	{
		BlockPos spawn = pos;
		
		while(!hasRoomForPlayer(world, spawn))
		{
			if(spawn.getY() > world.getHeight())
			{
				throw CommandBase.createException("Could not find safe position");
			}
			
			spawn = spawn.up();
		}
		
		return spawn;
	}
	
	protected static boolean hasRoomForPlayer(IBlockReader reader, BlockPos pos)
	{
		return Block.func_220064_c(reader, pos.down()) && !reader.getBlockState(pos).getMaterial().isSolid() && !reader.getBlockState(pos.up()).getMaterial().isSolid();
	}
	
	public static void doTeleport(MinecraftServer server, Entity entity, BlockPos pos, int dimension)
	{
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.5D;
		double z = pos.getZ() + 0.5D;
		
		if(entity instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity player = (ServerPlayerEntity) entity;
			
			player.removePassengers();
			player.teleport(server.getWorld(DimensionType.getById(dimension)), x, y, z, entity.rotationYaw, entity.rotationPitch);
			player.setRotationYawHead(entity.rotationYaw);
			
			player.connection.sendPacket(new SSetExperiencePacket(player.experience, player.experienceTotal, player.experienceLevel));
		}
		else
		{
			entity.setPosition(x, y, z);
		}
		
		if(!(entity instanceof LivingEntity) || !((LivingEntity) entity).isElytraFlying())
		{
			entity.setMotion(entity.getMotion().mul(1.0D, 0.0D, 1.0D));
			entity.onGround = true;
		}
	}
}
