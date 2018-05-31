package exopandora.kardexo.kardexotools;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		if(Config.HOME.getData().containsKey(sender.getName()))
		{
			Entity entity = getEntity(server, sender, sender.getName());
			
			if(entity.world != null)
			{
				Home home = Config.HOME.getData().get(sender.getName());
				doTeleport(entity, getPlayerSpawnPosition(entity.getEntityWorld(), home.getPosition()), home.getDimension());
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
	
	private static BlockPos getPlayerSpawnPosition(World world, BlockPos pos)
	{
		BlockPos spawn = pos;
		
		while(!hasRoomForPlayer(world, spawn))
		{
			spawn = spawn.up();
		}
		
        return spawn;
	}
	
	private static boolean hasRoomForPlayer(World world, BlockPos pos)
    {
        return world.getBlockState(pos.down()).isOpaqueCube() && !world.getBlockState(pos).getMaterial().isSolid() && !world.getBlockState(pos.up()).getMaterial().isSolid();
    }
	
	public static void doTeleport(Entity entity, BlockPos pos, int dimension)
	{ 
		if(entity instanceof EntityPlayerMP)
		{
			Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags>noneOf(SPacketPlayerPosLook.EnumFlags.class);
			entity.dismountRidingEntity();
			
			if(entity.dimension != dimension)
			{
				entity.changeDimension(dimension);
			}
			
			((EntityPlayerMP)entity).connection.setPlayerLocation(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, entity.rotationYaw, entity.rotationPitch, set);
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
}
