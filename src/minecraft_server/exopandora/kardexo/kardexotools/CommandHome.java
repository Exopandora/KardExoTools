package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CommandHome extends CommandBase
{
	@Override
	public String getName()
	{
		return "home";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/home";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		Home home = Config.HOME.getData().get(sender.getName());
		
		if(home != null)
		{
			Entity entity = getEntity(server, sender, sender.getName());
			
			if(entity.world != null)
			{
				try
				{
					doTeleport(server, entity, getPlayerSpawnPosition(server.getWorld(home.getDimension()), home.getPosition()), home.getDimension());
				}
				catch(InvalidSpawnPositionException e)
				{
					throw new CommandException(e.getMessage());
				}
			}
		}
		else
		{
			throw new CommandException("No home set");
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
	
	private static BlockPos getPlayerSpawnPosition(World world, BlockPos pos) throws InvalidSpawnPositionException
	{
		BlockPos spawn = pos;
		
		while(!hasRoomForPlayer(world, spawn))
		{
			if(spawn.getY() > world.getHeight())
			{
				throw new InvalidSpawnPositionException("Could not find safe position");
			}
			
			spawn = spawn.up();
		}
		
		return spawn;
	}
	
	private static boolean hasRoomForPlayer(World world, BlockPos pos)
	{
		return world.getBlockState(pos.down()).isTopSolid() && !world.getBlockState(pos).getMaterial().isSolid() && !world.getBlockState(pos.up()).getMaterial().isSolid();
	}
	
	public static void doTeleport(MinecraftServer server, Entity entity, BlockPos pos, int dimension)
	{ 
		if(entity instanceof EntityPlayerMP)
		{
			entity.dismountRidingEntity();
			entity.removePassengers();
			
			if(entity.dimension != dimension)
			{
				changeDimension((EntityPlayerMP) entity, server, dimension);
			}
			
			((EntityPlayerMP) entity).connection.setPlayerLocation(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, entity.rotationYaw, entity.rotationPitch);
		}
		else
		{
			entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, entity.rotationYaw, entity.rotationPitch);
		}
		
		if(!(entity instanceof EntityLivingBase) || !((EntityLivingBase)entity).isElytraFlying())
		{
			entity.motionY = 0.0D;
			entity.onGround = true;
		}
	}
	
	private static void changeDimension(EntityPlayerMP player, MinecraftServer server, int dimensionIn)
	{
		int lastDimension = player.dimension;
		PlayerList list = server.getPlayerList();
		
		WorldServer oldWorld = server.getWorld(player.dimension);
		player.dimension = dimensionIn;
		WorldServer toWorld = server.getWorld(player.dimension);
		
		player.connection.sendPacket(new SPacketRespawn(player.dimension, player.world.getDifficulty(), player.world.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
		list.updatePermissionLevel(player);
		oldWorld.removeEntityDangerously(player);
		player.isDead = false;
		transferEntityToWorld(player, lastDimension, oldWorld, toWorld);
		list.preparePlayer(player, oldWorld);
		
		player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		player.interactionManager.setWorld(toWorld);
		player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
		player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
		
		list.updateTimeAndWeatherForPlayer(player, toWorld);
		list.syncPlayerInventory(player);
		
		for(PotionEffect potioneffect : player.getActivePotionEffects())
		{
			player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
		}
		
		player.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
	}
	
	private static void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorld, WorldServer toWorld)
	{
		double x = entityIn.posX;
		double z = entityIn.posZ;
		
		if(lastDimension != 1)
		{
			oldWorld.profiler.startSection("placing");
			x = (double)MathHelper.clamp((int) x, -29999872, 29999872);
			z = (double)MathHelper.clamp((int) z, -29999872, 29999872);
			
			if(entityIn.isEntityAlive())
			{
				entityIn.setLocationAndAngles(x, entityIn.posY, z, entityIn.rotationYaw, entityIn.rotationPitch);
				toWorld.spawnEntity(entityIn);
				toWorld.updateEntityWithOptionalForce(entityIn, false);
			}
			
			oldWorld.profiler.endSection();
		}
		
		entityIn.setWorld(toWorld);
	}
}
