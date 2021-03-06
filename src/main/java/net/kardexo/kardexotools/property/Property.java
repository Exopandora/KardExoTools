package net.kardexo.kardexotools.property;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Property
{
	private final List<PropertyOwner> owners;
	private List<Property> children;
	private String name;
	private String title;
	private ResourceLocation dimension;
	private double xMin;
	private double zMin;
	private double xMax;
	private double zMax;
	@SerializedName("protected")
	private boolean isProtected;
	
	public Property(String name, String title, List<PropertyOwner> owners, ResourceLocation dimension, double xMin, double zMin, double xMax, double zMax)
	{
		this.name = name;
		this.title = title;
		this.owners = owners;
		this.dimension = dimension;
		this.xMin = xMin;
		this.zMin = zMin;
		this.xMax = xMax;
		this.zMax = zMax;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public double getXMin()
	{
		return this.xMin;
	}
	
	public void setXMin(int xMin)
	{
		this.xMin = xMin;
	}
	
	public double getZMin()
	{
		return this.zMin;
	}
	
	public void setZMin(int zMin)
	{
		this.zMin = zMin;
	}
	
	public double getXMax()
	{
		return this.xMax;
	}
	
	public void setXMax(int xMax)
	{
		this.xMax = xMax;
	}
	
	public double getZMax()
	{
		return this.zMax;
	}
	
	public void setZMax(int zMax)
	{
		this.zMax = zMax;
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
	
	public String getTitle()
	{
		return this.title != null ? this.title : this.name;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public boolean isProtected()
	{
		return isProtected;
	}
	
	public void setProtected(boolean isProtected)
	{
		this.isProtected = isProtected;
		
		if(this.children != null)
		{
			for(Property child : this.children)
			{
				child.setProtected(isProtected);
			}
		}
	}
	
	public void addChild(Property child)
	{
		if(this.children == null)
		{
			this.children = Lists.newArrayList();
		}
		
		this.children.add(child);
	}
	
	public void removeChild(Property child)
	{
		if(this.children != null)
		{
			this.children.remove(child);
		}
	}
	
	public List<String> getChildrenNames()
	{
		if(this.children != null)
		{
			return this.children.stream().map(Property::getName).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	public List<String> getChildrenTitles()
	{
		if(this.children != null)
		{
			return this.children.stream().map(Property::getTitle).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	public List<Property> getChildren()
	{
		return this.children;
	}
	
	public Property getChild(String name)
	{
		if(this.children != null)
		{
			for(Property child : this.children)
			{
				if(child.getName().equals(name))
				{
					return child;
				}
			}
		}
		
		return null;
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
		return pos.getX() >= this.xMin && pos.getX() <= this.xMax && pos.getZ() >= this.zMin && pos.getZ() <= this.zMax && dimension.equals(this.dimension);
	}
	
	public boolean isInsideChild(Player player)
	{
		return this.isInsideChild(player.blockPosition(), player.level.dimension().location());
	}
	
	public boolean isInsideChild(BlockPos pos, ResourceLocation dimension)
	{
		if(this.children != null)
		{
			return this.children.stream().anyMatch(child -> child != null && child.isInside(pos, dimension));
		}
		
		return false;
	}
	
	public List<PropertyOwner> getAllOwners()
	{
		return this.owners;
	}
	
	public void addOwner(PropertyOwner owner)
	{
		this.owners.add(owner);
	}
	
	public void removeOwner(PropertyOwner owner)
	{
		this.owners.remove(owner);
	}
	
	public void hasOwner(PropertyOwner owner)
	{
		this.owners.contains(owner);
	}
	
	public List<PropertyOwner> getOwners()
	{
		return this.owners.stream().filter(owner -> !owner.isCreator()).collect(Collectors.toList());
	}
	
	public boolean isOwner(String username)
	{
		return this.owners.stream().anyMatch(owner -> owner.getName().equals(username));
	}
	
	public List<PropertyOwner> getCreators()
	{
		return this.owners.stream().filter(PropertyOwner::isCreator).collect(Collectors.toList());
	}
	
	public boolean isCreator(String username)
	{
		return this.owners.stream().filter(PropertyOwner::isCreator).anyMatch(owner -> owner.getName().equals(username));
	}
	
	public String getOwners(String delimiter)
	{
		return String.join(delimiter, this.owners.stream().filter(owner -> !owner.isCreator()).map(PropertyOwner::getName).collect(Collectors.toList()));
	}
	
	public String getCreators(String delimiter)
	{
		return String.join(delimiter, this.getCreators().stream().map(PropertyOwner::getName).collect(Collectors.toList()));
	}
	
	public String getChildren(String delimiter)
	{
		return String.join(delimiter, this.getChildrenTitles());
	}
	
	public double getSize()
	{
		return this.getMainSize() + this.getChildrenSize();
	}
	
	public double getMainSize()
	{
		return (this.getXMax() - this.getXMin() + 1) * (this.getZMax() - this.getZMin() + 1);
	}
	
	public double getChildrenSize()
	{
		if(this.children != null)
		{
			return this.children.stream().mapToDouble(child -> child != null ? child.getSize() : 0D).sum();
		}
		
		return 0D;
	}
	
	public MutableComponent getDisplayName()
	{
		StringBuilder builder = new StringBuilder(this.getTitle());
		
		if(this.title != null)
		{
			builder.append("\nName: " + this.name);
		}
		
		builder.append("\nCreators: " + this.getCreators(", "));
		
		if(!this.getOwners().isEmpty())
		{
			builder.append("\nOwners: " + this.getOwners(", "));
		}
		
		builder.append("\nDimension: " + this.dimension);
		
		if(this.children != null)
		{
			builder.append("\nChildren: " + this.getChildren(","));
		}
		
		builder.append("\nX: [" + this.xMin + ", " + this.xMax + "]");
		builder.append("\nZ: [" + this.zMin + ", " + this.zMax + "]");
		builder.append("\nSize: " + this.getSize());
		builder.append("\nProtected: " + this.isProtected);
		
		MutableComponent basetextcomponent = new TextComponent(this.getTitle());
		Style style = Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(builder.toString())))
				.withInsertion(this.name);
		basetextcomponent.setStyle(style);
		
		return basetextcomponent;
	}
}
