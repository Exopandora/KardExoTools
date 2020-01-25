package exopandora.kardexo.kardexotools.base;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;

public class Property
{
	private final List<PropertyOwner> owners;
	private List<Property> children;
	private String name;
	private String title;
	private int dimension;
	private double xMin;
	private double zMin;
	private double xMax;
	private double zMax;
	
	public Property(String name, String title, List<PropertyOwner> owners, int dimension, double xMin, double zMin, double xMax, double zMax)
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
	
	public int getDimension()
	{
		return this.dimension;
	}
	
	public void setDimension(int dimension)
	{
		this.dimension = dimension;
	}
	
	public String getTitle()
	{
		return this.title != null ? this.title : this.name;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
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
			return this.children.parallelStream().map(Property::getName).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
	
	public List<String> getChildrenTitles()
	{
		if(this.children != null)
		{
			return this.children.parallelStream().map(Property::getTitle).collect(Collectors.toList());
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
	
	public boolean isInside(PlayerEntity player)
	{
		return this.isInsideMain(player) || this.isInsideChild(player);
	}
	
	public boolean isInsideMain(PlayerEntity player)
	{
		float posX = MathHelper.floor(player.getPosX());
		float posZ = MathHelper.floor(player.getPosZ());
		
		return posX >= this.xMin && posX <= this.xMax && posZ >= this.zMin && posZ <= this.zMax && player.dimension.getId() == this.dimension;
	}
	
	public boolean isInsideChild(PlayerEntity player)
	{
		if(this.children != null)
		{
			for(Property child : this.children)
			{
				if(child != null)
				{
					if(child.isInside(player))
					{
						return true;
					}
				}
			}
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
		return this.owners.parallelStream().filter(owner -> !owner.isCreator()).collect(Collectors.toList());
	}
	
	public boolean isOwner(String username)
	{
		return this.owners.parallelStream().anyMatch(owner -> owner.getName().equals(username));
	}
	
	public List<PropertyOwner> getCreators()
	{
		return this.owners.parallelStream().filter(PropertyOwner::isCreator).collect(Collectors.toList());
	}
	
	public boolean isCreator(String username)
	{
		return this.owners.parallelStream().filter(PropertyOwner::isCreator).allMatch(owner -> owner.getName().equals(username));
	}
	
	public String getOwners(String delimiter)
	{
		return String.join(delimiter, this.owners.parallelStream().filter(owner -> !owner.isCreator()).map(PropertyOwner::getName).collect(Collectors.toList()));
	}
	
	public String getCreators(String delimiter)
	{
		return String.join(delimiter, this.getCreators().parallelStream().map(PropertyOwner::getName).collect(Collectors.toList()));
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
		double size = 0;
		
		if(this.children != null)
		{
			for(Property child : this.children)
			{
				if(child != null)
				{
					size += child.getSize();
				}
			}
		}
		
		return size;
	}
	
	public ITextComponent getDisplayName()
	{
		ITextComponent basetextcomponent = new StringTextComponent(this.getTitle());
		basetextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(this.getTitle() + (this.title != null ? "\nName: " + this.name : "") + "\nCreators: " + this.getCreators(", ") + "\n" + (!this.getOwners().isEmpty() ? ("Owners: " + this.getOwners(", ") + "\n") : "") + "Dimension: " + DimensionType.getById(this.dimension).getSuffix() + "\n" + (this.children != null ? ("Children: " + this.getChildren(",") + "\n") : "") +  "X: [" + this.xMin + ", " + this.xMax + "]\nZ: [" + this.zMin + ", " + this.zMax + "]\nSize: " + this.getSize())));
		basetextcomponent.getStyle().setInsertion(this.name);
		return basetextcomponent;
	}
}
