package net.kardexo.kardexotools.property;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.DataFile;
import net.kardexo.kardexotools.tasks.TickableBases;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PropertyHelper
{
	public static void add(String id, ServerLevel dimension, ColumnPos from, ColumnPos to, String owner, String title, DataFile<Property, String> data) throws IllegalStateException
	{
		if(data.containsKey(id))
		{
			throw new IllegalStateException();
		}
		
		double xMin = Math.min(from.x, to.x);
		double zMin = Math.min(from.z, to.z);
		double xMax = Math.max(from.x, to.x);
		double zMax = Math.max(from.z, to.z);
		
		Property property = new Property(id, title, Lists.newArrayList(new PropertyOwner(owner, true, true, null, null)), dimension.dimension().location(), xMin, zMin, xMax, zMax);
		
		data.put(id, property);
		data.save();
	}
	
	public static void remove(String id, DataFile<Property, String> data) throws NoSuchElementException
	{
		Property property = data.get(id);
		
		if(property != null)
		{
			TickableBases.remove(property);
			data.remove(id);
			data.save();
		}
		else
		{
			throw new NoSuchElementException();
		}
	}
	
	public static void addChild(Property parent, String id, ServerLevel dimension, ColumnPos from, ColumnPos to, String title, DataFile<Property, String> data) throws IllegalStateException
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
		data.save();
	}
	
	public static void removeChild(Property parent, String id, DataFile<Property, String> data) throws NoSuchElementException
	{
		Property child = parent.getChild(id);
		
		if(child == null)
		{
			throw new NoSuchElementException();
		}
		
		parent.removeChild(child);
		data.save();
	}
	
	public static boolean isCreator(String name, String id, Map<String, Property> data)
	{
		Property property = data.get(id);
		
		if(property != null)
		{
			return property.isCreator(name);
		}
		
		return false;
	}
	
	public static boolean isOwner(String name, String id, Map<String, Property> data)
	{
		Property property = data.get(id);
		
		if(property != null)
		{
			return property.isOwner(name);
		}
		
		return false;
	}
	
	public static PropertyOwner getOwner(String id, String name, Map<String, Property> data)
	{
		for(PropertyOwner owner : data.get(id).getAllOwners())
		{
			if(owner.getName().equals(name))
			{
				return owner;
			}
		}
		
		return null;
	}
	
	public static void forOwner(String id, Player player, Map<String, Property> data, Consumer<PropertyOwner> callback)
	{
		PropertyOwner owner = getOwner(id, player.getGameProfile().getName(), data);
		
		if(owner != null && callback != null)
		{
			callback.accept(owner);
		}
	}
	
	public static boolean hasPermission(CommandSourceStack source, String id, Player target, Map<String, Property> data)
	{
		return source.hasPermission(4) || isCreator(source.getTextName(), id, data) || target != null && isOwner(source.getTextName(), id, data) && target.equals(source.getEntity());
	}
	
	public static Property getProperty(String id, Map<String, Property> data) throws NoSuchElementException
	{
		Property property = data.get(id);
		
		if(property == null)
		{
			throw new NoSuchElementException();
		}
		
		return property;
	}
	
	private static boolean isProtected(Player player, BlockPos pos, Map<String, Property> data)
	{
		String name = player.getGameProfile().getName();
		
		for(Property property : data.values())
		{
			if(property.isProtected() && !property.isOwner(name) && property.isInside(pos, player.level.dimension().location()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isProtected(Player player, BlockPos pos)
	{
		return isProtected(player, pos, KardExo.BASES) || isProtected(player, pos, KardExo.PLACES);
	}
	
	private static boolean isProtected(Player player, Entity entity)
	{
		return isProtected(player, entity.blockPosition(), KardExo.BASES) || isProtected(player, entity.blockPosition(), KardExo.PLACES);
	}
	
	public static boolean canHarvestBlock(Player player, BlockPos pos)
	{
		return !isProtected(player, pos);
	}
	
	public static boolean canInteractWithEntity(Player player, Entity entity)
	{
		return entity instanceof Monster && !entity.hasCustomName() || !isProtected(player, entity);
	}
	
	public static boolean cancelBlockInteraction(ServerPlayer player)
	{
		player.getServer().getPlayerList().sendAllPlayerInfo(player);
		return true;
	}
}
