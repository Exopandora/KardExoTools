package net.kardexo.kardexotools.util;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.MapFile;
import net.kardexo.kardexotools.config.OwnerConfig;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.tasks.BasesTickable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;

public class PropertyUtils
{
	public static void add(String id, ServerLevel dimension, BoundingBox boundingBox, @Nullable UUID owner, @Nullable Component displayName, MapFile<String, Property> data) throws IllegalStateException
	{
		if(data.containsKey(id))
		{
			throw new IllegalStateException();
		}
		
		Map<UUID, OwnerConfig> owners = null;
		
		if(owner != null)
		{
			owners = new HashMap<UUID, OwnerConfig>();
			owners.put(owner, new OwnerConfig(true, true, null, null));
		}
		
		Property property = new Property(displayName, owners, dimension.dimension().location(), boundingBox);
		data.put(id, property);
		data.save();
	}
	
	public static void remove(String id, MapFile<String, Property> data) throws NoSuchElementException
	{
		Property property = data.get(id);
		
		if(property == null)
		{
			throw new NoSuchElementException();
		}
		
		BasesTickable.remove(property);
		data.remove(id);
		data.save();
	}
	
	public static void addChild(Property parent, String id, ServerLevel dimension, BoundingBox boundingBox, Component title, MapFile<String, Property> data) throws IllegalStateException
	{
		Property property = new Property(title, null, dimension.dimension().location(), boundingBox);
		
		if(property.getChild(id) != null)
		{
			throw new IllegalStateException();
		}
		
		parent.addChild(id, property);
		data.save();
	}
	
	public static void removeChild(Property parent, String id, MapFile<String, Property> data) throws NoSuchElementException
	{
		Property child = parent.getChild(id);
		
		if(child == null)
		{
			throw new NoSuchElementException();
		}
		
		parent.removeChild(id);
		data.save();
	}
	
	public static boolean isCreator(UUID uuid, String id, Map<String, Property> data)
	{
		Property property = data.get(id);
		
		if(property == null)
		{
			return false;
		}
		
		return property.isCreator(uuid);
	}
	
	public static boolean isOwner(UUID uuid, String id, Map<String, Property> data)
	{
		Property property = data.get(id);
		
		if(property == null)
		{
			return false;
		}
		
		return property.isOwner(uuid);
	}
	
	public static OwnerConfig getOwnerConfig(String id, UUID uuid, Map<String, Property> data)
	{
		Property property = data.get(id);
		
		if(property == null)
		{
			return null;
		}
		
		for(Entry<UUID, OwnerConfig> owner : property.getOwners().entrySet())
		{
			if(Objects.equal(owner.getKey(), uuid))
			{
				return owner.getValue();
			}
		}
		
		return null;
	}
	
	public static boolean hasPermission(CommandSourceStack source, String id, Player target, Map<String, Property> data)
	{
		return source.hasPermission(4) || isCreator(CommandUtils.getUUID(source), id, data) || target != null && isOwner(CommandUtils.getUUID(source), id, data) && target.equals(source.getEntity());
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
		UUID uuid = player.getUUID();
		
		for(Property property : data.values())
		{
			if(property.isProtected() && !property.isOwner(uuid) && property.isInside(pos, player.level().dimension().location()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isProtected(Player player, BlockPos pos)
	{
		return isProtected(player, pos, KardExo.BASES.getData()) || isProtected(player, pos, KardExo.PLACES.getData());
	}
	
	private static boolean isProtected(Player player, Entity entity)
	{
		return isProtected(player, entity.blockPosition(), KardExo.BASES.getData()) || isProtected(player, entity.blockPosition(), KardExo.PLACES.getData());
	}
	
	public static boolean canHarvestBlock(Player player, BlockPos pos)
	{
		return !isProtected(player, pos);
	}
	
	public static boolean canInteractWithEntity(Player player, Entity entity)
	{
		return entity instanceof Monster && !entity.hasCustomName() || !isProtected(player, entity);
	}
}
