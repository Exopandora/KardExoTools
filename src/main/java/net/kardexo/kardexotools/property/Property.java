package net.kardexo.kardexotools.property;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;

import net.kardexo.kardexotools.config.OwnerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class Property
{
	@Nullable
	@SerializedName("display_name")
	private Component displayName;
	@Nullable
	@SerializedName("owners")
	private Map<UUID, OwnerConfig> owners;
	@SerializedName("dimension")
	private ResourceLocation dimension;
	@SerializedName("bounds")
	private BoundingBox boundingBox;
	@SerializedName("protected")
	private boolean isProtected;
	@Nullable
	@SerializedName("children")
	private Map<String, Property> children;
	
	public Property(@Nullable Component displayName, @Nullable Map<UUID, OwnerConfig> owners, ResourceLocation dimension, BoundingBox boundingBox)
	{
		this.displayName = displayName;
		this.owners = owners;
		this.dimension = dimension;
		this.boundingBox = boundingBox;
	}
	
	public BoundingBox getBoundingBox()
	{
		return this.boundingBox;
	}
	
	public void setBoundingBox(BoundingBox boundingBox)
	{
		this.boundingBox = boundingBox;
	}
	
	public ResourceKey<Level> getDimension()
	{
		return ResourceKey.create(Registry.DIMENSION_REGISTRY, this.dimension);
	}
	
	public void setDimension(ResourceLocation dimension)
	{
		this.dimension = dimension;
	}
	
	public void setDimension(ResourceKey<Level> dimension)
	{
		this.dimension = dimension.location();
	}
	
	public MutableComponent getDisplayName(String id)
	{
		return this.displayName != null ? this.displayName.copy() : new TextComponent(id);
	}
	
	public void setDisplayName(Component displayName)
	{
		this.displayName = displayName;
	}
	
	public boolean isProtected()
	{
		return this.isProtected;
	}
	
	public void setProtected(boolean isProtected)
	{
		this.isProtected = isProtected;
		
		if(this.children != null)
		{
			for(Property child : this.children.values())
			{
				child.setProtected(isProtected);
			}
		}
	}
	
	public void addChild(String id, Property child)
	{
		if(this.children == null)
		{
			this.children = Maps.newHashMap();
		}
		
		this.children.put(id, child);
	}
	
	public void removeChild(String id)
	{
		if(this.children != null)
		{
			this.children.remove(id);
		}
	}
	
	public Set<String> getChildrenIds()
	{
		if(this.children == null)
		{
			return Collections.emptySet();
		}
		
		return this.children.keySet();
	}
	
	public Map<String, Property> getChildren()
	{
		if(this.children == null)
		{
			return Collections.emptyMap();
		}
		
		return Collections.unmodifiableMap(this.children);
	}
	
	@Nullable
	public Property getChild(String id)
	{
		if(this.children == null)
		{
			return null;
		}
		
		return this.children.get(id);
	}
	
	public boolean isInside(Player player)
	{
		return this.isInside(player.blockPosition(), player.level.dimension().location());
	}
	
	public boolean isInside(BlockPos pos, ResourceLocation dimension)
	{
		return this.isInsideMain(pos, dimension) || this.isInsideChild(pos, dimension);
	}
	
	public boolean isInsideMain(Player player)
	{
		return this.isInsideMain(player.blockPosition(), player.level.dimension().location());
	}
	
	public boolean isInsideMain(BlockPos pos, ResourceLocation dimension)
	{
		return this.boundingBox.isInside(pos) && dimension.equals(this.dimension);
	}
	
	public boolean isInsideChild(Player player)
	{
		return this.isInsideChild(player.blockPosition(), player.level.dimension().location());
	}
	
	public boolean isInsideChild(BlockPos pos, ResourceLocation dimension)
	{
		if(this.children == null)
		{
			return false;
		}
		
		return this.children.values().stream().anyMatch(child -> child != null && child.isInside(pos, dimension));
	}
	
	public Map<UUID, OwnerConfig> getOwners()
	{
		if(this.owners.isEmpty())
		{
			return Collections.emptyMap();
		}
		
		return Collections.unmodifiableMap(this.owners);
	}
	
	public void putOwner(UUID uuid, OwnerConfig config)
	{
		if(this.owners == null)
		{
			this.owners = Maps.newHashMap();
		}
		
		this.owners.put(uuid, config);
	}
	
	public void removeOwner(UUID uuid)
	{
		if(this.owners != null)
		{
			this.owners.remove(uuid);
		}
	}
	
	public boolean isCreator(UUID uuid)
	{
		if(this.owners == null)
		{
			return false;
		}
		
		return this.owners.entrySet().stream().anyMatch(entry -> entry.getValue().isCreator() && Objects.equal(entry.getKey(), uuid));
	}
	
	public boolean isOwner(UUID uuid)
	{
		if(this.owners == null)
		{
			return false;
		}
		
		return this.owners.keySet().stream().anyMatch(owner -> Objects.equal(owner, uuid));
	}
	
	private MutableComponent getDisplayTooltip(String id, GameProfileCache profileCache)
	{
		MutableComponent metadata = new TextComponent("\n" + id).withStyle(ChatFormatting.GRAY);
		
		if(this.owners != null && !this.owners.isEmpty())
		{
			metadata.append("\nOwners: ").append(listOwners(this.owners, profileCache));
		}
		
		metadata.append("\nDimension: " + this.dimension);
		metadata.append("\nFrom: ").append(formatCoordinate(new BlockPos(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ())));
		metadata.append("\nTo: ").append(formatCoordinate(new BlockPos(this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ())));
		metadata.append("\nProtected: " + this.isProtected);
		
		if(this.children != null && !this.children.isEmpty())
		{
			metadata.append("\nChildren: ").append(listChildren(this.children));
		}
		
		return this.getDisplayName(id).append(metadata);
	}
	
	public MutableComponent getDisplayName(String id, GameProfileCache profileCache)
	{
		return this.getDisplayName(id).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.getDisplayTooltip(id, profileCache))).withInsertion(id));
	}
	
	private static Component listOwners(Map<UUID, OwnerConfig> owners, GameProfileCache profileCache)
	{
		return ComponentUtils.formatList(owners.entrySet(), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR, entry ->
		{
			MutableComponent result = new TextComponent(profileCache.get(entry.getKey()).map(GameProfile::getName).orElse(entry.getKey().toString()));
			
			if(entry.getValue().isCreator())
			{
				result.withStyle(ChatFormatting.AQUA);
			}
			
			return result;
		});
	}
	
	private static Component listChildren(Map<String, Property> children)
	{
		return ComponentUtils.formatList(children.entrySet(), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR, entry -> entry.getValue().getDisplayName(entry.getKey()));
	}
	
	private static MutableComponent formatCoordinate(BlockPos pos)
	{
		return ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", new Object[]{pos.getX(), pos.getY(), pos.getZ()}));
	}
}
