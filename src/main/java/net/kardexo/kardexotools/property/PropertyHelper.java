package net.kardexo.kardexotools.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.config.DataFile;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.client.CUseEntityPacket.Action;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.dimension.DimensionType;

public class PropertyHelper
{
	public static void add(String id, DimensionType dimension, ColumnPos from, ColumnPos to, String owner, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		List<PropertyOwner> owners = new ArrayList<PropertyOwner>();
		owners.add(new PropertyOwner(owner, true, true, null, null));
		
		Property property = new Property(id, title, owners, dimension.getId(), xMin, zMin, xMax, zMax);
		
		if(file.getData().containsKey(property.getName()))
		{
			throw new IllegalStateException();
		}
		
		file.getData().put(property.getName(), property);
		file.save();
	}
	
	public static void remove(String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		if(file.getData().containsKey(id))
		{
			file.getData().remove(id);
			file.save();
		}
		
		throw new NoSuchElementException();
	}
	
	public static void addChild(Property parent, String id, DimensionType dimension, ColumnPos from, ColumnPos to, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		Property property = new Property(id, title, Collections.emptyList(), dimension.getId(), xMin, zMin, xMax, zMax);
		
		if(property.getChild(id) != null)
		{
			throw new IllegalStateException();
		}
		
		parent.addChild(property);
		file.save();
	}
	
	public static void removeChild(Property parent, String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		Property child = parent.getChild(id);
		
		if(child == null)
		{
			throw new NoSuchElementException();
		}
		
		parent.removeChild(child);
		file.save();
	}
	
	public static boolean isCreator(String name, String id, DataFile<Property, String> file)
	{
		Property property = file.getData().get(id);
		
		if(property != null)
		{
			return property.isCreator(name);
		}
		
		return false;
	}
	
	public static boolean isOwner(String name, String id, DataFile<Property, String> file)
	{
		Property property = file.getData().get(id);
		
		if(property != null)
		{
			return property.isOwner(name);
		}
		
		return false;
	}
	
	public static PropertyOwner getOwner(String id, String name, DataFile<Property, String> file)
	{
		for(PropertyOwner owner : file.getData().get(id).getAllOwners())
		{
			if(owner.getName().equals(name))
			{
				return owner;
			}
		}
		
		return null;
	}
	
	public static void forOwner(String id, PlayerEntity player, DataFile<Property, String> file, Consumer<PropertyOwner> callback)
	{
		PropertyOwner owner = getOwner(id, player.getGameProfile().getName(), file);
		
		if(owner != null && callback != null)
		{
			callback.accept(owner);
		}
	}
	
	public static boolean hasPermission(CommandSource source, String id, PlayerEntity target, DataFile<Property, String> file)
	{
		return source.hasPermissionLevel(4) || isCreator(source.getName(), id, file) || target != null && isOwner(source.getName(), id, file) && target.equals(source.getEntity());
	}
	
	public static Property getProperty(String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		Property property = file.getData().get(id);
		
		if(property == null)
		{
			throw new NoSuchElementException();
		}
		
		return property;
	}
	
	private static boolean isProtected(PlayerEntity player, BlockPos pos, DataFile<Property, String> file)
	{
		String name = player.getGameProfile().getName();
		
		for(Property property : file.getData().values())
		{
			if(property.isProtected() && !property.isOwner(name) && property.isInside(pos, player.dimension.getId()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isProtected(PlayerEntity player, BlockPos pos)
	{
		return isProtected(player, pos, Config.BASES) || isProtected(player, pos, Config.PLACES);
	}
	
	private static boolean isProtected(PlayerEntity player, Entity entity)
	{
		return isProtected(player, entity.getPosition(), Config.BASES) || isProtected(player, entity.getPosition(), Config.PLACES);
	}
	
	public static boolean canHarvestBlock(PlayerEntity player, BlockPos pos)
	{
		return !isProtected(player, pos);
	}
	
	public static boolean canInteractWithBlock(PlayerEntity player, BlockPos pos, BlockState blockstate)
	{
		return !(blockstate.getBlock() instanceof ContainerBlock) || !isProtected(player, pos);
	}
	
	public static boolean canPlaceBlock(PlayerEntity player, BlockPos pos, BlockState blockstate)
	{
		return !isProtected(player, pos);
	}
	
	public static boolean canInteractWithEntity(PlayerEntity player, Entity entity, Action action)
	{
		return entity instanceof MonsterEntity || action != Action.ATTACK || !isProtected(player, entity);
	}
	
	public static ActionResultType cancelBlockInteraction(ServerPlayerEntity player)
	{
		KardExo.getServer().getPlayerList().sendInventory(player);
		return ActionResultType.PASS;
	}
}
