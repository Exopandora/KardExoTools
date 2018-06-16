package exopandora.kardexo.kardexotools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;

public abstract class CommandProperty extends CommandBase
{
	protected final DataFile<Property, String> file;
	
	public CommandProperty(DataFile<Property, String> file)
	{
		this.file = file;
	}
	
	public void list(ICommandSender sender) throws CommandException, NoSuchElementException
	{
		if(this.file.getData().values().isEmpty())
		{
			throw new NoSuchElementException();
		}
		
		this.list(sender, this.file.getData().values(), "");
	}
	
	private void list(ICommandSender sender, Collection<Property> list, String indentation) throws CommandException, NoSuchElementException
	{
		for(Property property : list)
		{
			sender.sendMessage(new TextComponentTranslation(indentation + (indentation.isEmpty() ? "Name" : "Child") + ": %s", new Object[]{property.getDisplayName()}));
			
			String creators = property.getCreators(", ");
			
			if(!creators.isEmpty())
			{
				sender.sendMessage(new TextComponentString(indentation + " Creators: " + creators));
			}
			
			String owners = property.getOwners(", ");
			
			if(!owners.isEmpty())
			{
				sender.sendMessage(new TextComponentString(indentation + " Owners: " + owners));
			}
			
			sender.sendMessage(new TextComponentString(indentation + " X: [" + property.getXMin() + ", " + property.getXMax() + "]"));
			sender.sendMessage(new TextComponentString(indentation + " Z: [" + property.getZMin() + ", " + property.getZMax() + "]"));
			
			if(property.getChildren() != null)
			{
				this.list(sender, property.getChildren(), " " + indentation);
			}
		}
	}
	
	public void add(String[] args, PropertyOwner owner, int nameIndex, int dimIndex, int x1Index, int z1Index, int x2Index, int z2Index, int titleIndex) throws IllegalArgumentException, NumberInvalidException, IllegalStateException
	{
		if(!this.file.getData().containsKey(args[nameIndex]))
		{
			this.file.getData().put(args[nameIndex], this.createProperty(args, Lists.newArrayList(owner), nameIndex, dimIndex, x1Index, z1Index, x2Index, z2Index, titleIndex));
			this.file.save();
		}
		else
		{
			throw new IllegalStateException();
		}
	}
	
	public void addChild(Property parent, String[] args, int nameIndex, int dimIndex, int x1Index, int z1Index, int x2Index, int z2Index, int titleIndex) throws IllegalArgumentException, NumberInvalidException, IllegalStateException
	{
		if(parent.getChild(args[nameIndex]) == null)
		{
			parent.addChild(this.createProperty(args, Collections.emptyList(), nameIndex, dimIndex, x1Index, z1Index, x2Index, z2Index, titleIndex));
			this.file.save();
		}
		else
		{
			throw new IllegalStateException();
		}
	}
	
	private Property createProperty(String[] args, List<PropertyOwner> owners, int nameIndex, int dimIndex, int x1Index, int z1Index, int x2Index, int z2Index, int titleIndex) throws IllegalArgumentException, NumberInvalidException, IllegalStateException
	{
		int dimension = DimensionType.byName(args[dimIndex]).getId();
		double x1 = super.parseDouble(args[x1Index]);
		double z1 = super.parseDouble(args[z1Index]);
		double x2 = super.parseDouble(args[x2Index]);
		double z2 = super.parseDouble(args[z2Index]);
		
		String title = null;
		
		if(args.length > titleIndex)
		{
			title = String.join(" ", Arrays.copyOfRange(args, titleIndex, args.length));
		}
		
		return new Property(args[nameIndex], title, owners, dimension, Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2));
	}
	
	public void remove(String property, ICommandSender sender, MinecraftServer server) throws PermissionException, NoSuchElementException
	{
		if(this.file.getData().containsKey(property))
		{
			if(this.checkPermission(property, sender.getName(), server))
			{
				this.file.getData().remove(property);
				this.file.save();
			}
			else
			{
				throw new PermissionException();
			}
		}
		else
		{
			throw new NoSuchElementException();
		}
	}
	
	public void reload(ICommandSender sender, MinecraftServer server) throws CommandException
	{
		if(this.checkOp(sender.getName(), server))
		{
			TickableBases.BASE_VISITORS.clear();
			this.file.read();
		}
		else
		{
			throw new CommandException("commands.generic.permission", new Object[0]);
		}
	}
	
	protected void forOwnerOfBase(String base, String name, Consumer<PropertyOwner> consumer)
	{
		for(PropertyOwner owner : this.file.getData().get(base).getAllOwners())
		{
			if(owner.getName().equals(name))
			{
				consumer.accept(owner);
				return;
			}
		}
	}
	
	protected boolean verifyOwnerSilently(String property, String name, MinecraftServer server)
	{
		try
		{
			return this.file.getData().get(property).isOwner(name);
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	protected boolean verifyOwner(String property, String name, MinecraftServer server) throws PermissionException
	{
		if(this.file.getData().get(property).isOwner(name))
		{
			return true;
		}
		
		throw new PermissionException(name + " is not an owner of base with id " + property);
	}
	
	protected boolean verifyCreator(String property, String name, MinecraftServer server)
	{
		return this.file.getData().get(property).getCreators().contains(new PropertyOwner(name));
	}
	
	protected boolean checkOp(String name, MinecraftServer server)
	{
		return Arrays.asList(server.getPlayerList().getOppedPlayerNames()).parallelStream().anyMatch(op -> op.equals(name)) || name.equals("Server");
	}
	
	protected boolean checkPermission(String base, String name, MinecraftServer server)
	{
		return this.verifyCreator(base, name, server) || this.checkOp(name, server);
	}
}
