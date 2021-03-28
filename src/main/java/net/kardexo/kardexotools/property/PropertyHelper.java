package net.kardexo.kardexotools.property;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.config.DataFile;
import net.kardexo.kardexotools.tasks.TickableBases;
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
import net.minecraft.world.server.ServerWorld;

public class PropertyHelper
{
	public static void add(String id, ServerWorld dimension, ColumnPos from, ColumnPos to, String owner, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		if(file.getData().containsKey(id))
		{
			throw new IllegalStateException();
		}
		
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		Property property = new Property(id, title, Lists.newArrayList(new PropertyOwner(owner, true, true, null, null)), dimension.dimension().location(), xMin, zMin, xMax, zMax);
		
		file.getData().put(id, property);
		Config.save(file);
	}
	
	public static void remove(String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		Property property = file.getData().get(id);
		
		if(property != null)
		{
			TickableBases.remove(property);
			file.getData().remove(id);
			Config.save(file);
		}
		else
		{
			throw new NoSuchElementException();
		}
	}
	
	public static void addChild(Property parent, String id, ServerWorld dimension, ColumnPos from, ColumnPos to, String title, DataFile<Property, String> file) throws IllegalStateException
	{
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		Property property = new Property(id, title, Collections.emptyList(), dimension.dimension().location(), xMin, zMin, xMax, zMax);
		
		if(property.getChild(id) != null)
		{
			throw new IllegalStateException();
		}
		
		parent.addChild(property);
		Config.save(file);
	}
	
	public static void removeChild(Property parent, String id, DataFile<Property, String> file) throws NoSuchElementException
	{
		Property child = parent.getChild(id);
		
		if(child == null)
		{
			throw new NoSuchElementException();
		}
		
		parent.removeChild(child);
		Config.save(file);
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
		return source.hasPermission(4) || isCreator(source.getTextName(), id, file) || target != null && isOwner(source.getTextName(), id, file) && target.equals(source.getEntity());
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
			if(property.isProtected() && !property.isOwner(name) && property.isInside(pos, player.level.dimension().location()))
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
		return isProtected(player, entity.blockPosition(), Config.BASES) || isProtected(player, entity.blockPosition(), Config.PLACES);
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
		return entity instanceof MonsterEntity && !entity.hasCustomName() || action != Action.ATTACK || !isProtected(player, entity);
	}
	
	public static ActionResultType cancelBlockInteraction(ServerPlayerEntity player)
	{
		player.getServer().getPlayerList().sendAllPlayerInfo(player);
		return ActionResultType.PASS;
	}
}
