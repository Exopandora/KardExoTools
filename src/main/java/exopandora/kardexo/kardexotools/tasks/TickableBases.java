package exopandora.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exopandora.kardexo.kardexotools.base.EnumBaseAccess;
import exopandora.kardexo.kardexotools.base.Property;
import exopandora.kardexo.kardexotools.base.PropertyOwner;
import exopandora.kardexo.kardexotools.data.Config;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TickableBases implements ITickable
{
	private final MinecraftServer server;
	public static final Map<Property, Set<String>> BASE_VISITORS = new HashMap<Property, Set<String>>();
	
	public TickableBases(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void tick()
	{
		for(Property base : Config.BASES.getData().values())
		{
			List<PropertyOwner> notifyList = new ArrayList<PropertyOwner>();
			
			for(PropertyOwner owner : base.getAllOwners())
			{
				if(owner.doNotify())
				{
					notifyList.add(owner);
				}
			}
			
			for(EntityPlayerMP player : this.server.getPlayerList().getPlayers())
			{
				boolean inside = base.isInside(player);
				
				Set<String> visitors = BASE_VISITORS.get(base);
				
				if(visitors == null)
				{
					visitors = new HashSet<String>();
				}
				
				if(visitors.contains(player.getName().getString()))
				{
					if(!inside)
					{
						visitors.remove(player.getName().getString());
						BASE_VISITORS.put(base, visitors);
						this.notifyOwners(base, notifyList, player, EnumBaseAccess.LEAVE);
					}
				}
				else
				{
					if(inside)
					{
						visitors.add(player.getName().getString());
						BASE_VISITORS.put(base, visitors);
						this.notifyOwners(base, notifyList, player, EnumBaseAccess.ENTER);
					}
				}
			}
		}
	}
	
	private void notifyOwners(Property base, List<PropertyOwner> notify, EntityPlayerMP player, EnumBaseAccess access)
	{
		if(!base.isOwner(player.getName().getString()))
		{
			switch(access)
			{
				case ENTER:
					this.server.logInfo(player.getName().getString() + " has entered base with id " + base.getTitle());
					break;
				case LEAVE:
					this.server.logInfo(player.getName().getString() + " has left base with id " + base.getTitle());
					break;
			}
			
			for(PropertyOwner owner : notify)
			{
				EntityPlayerMP playerOwner = this.server.getPlayerList().getPlayerByUsername(owner.getName());
				
				if(playerOwner != null)
				{
					playerOwner.sendMessage(this.getFormattedMessage(player, base, owner, access));
				}
			}
		}
	}
	
	private ITextComponent getFormattedMessage(EntityPlayerMP player, Property base, PropertyOwner owner, EnumBaseAccess access)
	{
		String format = null;
		
		switch(access)
		{
			case ENTER:
				format = owner.getEnterMessage();
				break;
			case LEAVE:
				format = owner.getExitMessage();
				break;
		}
		
		if(format != null)
		{
			return new TextComponentTranslation(format.replace("&name", "%1$s").replace("&base", "%2$s"), new Object[]{player.getDisplayName(), base.getDisplayName()});
		}
		
		switch(access)
		{
			case ENTER:
				return new TextComponentTranslation(PropertyOwner.getDefaultEnterMessage(), new Object[]{player.getDisplayName(), base.getDisplayName()});
			case LEAVE:
				return new TextComponentTranslation(PropertyOwner.getDefaultExitMessage(), new Object[]{player.getDisplayName(), base.getDisplayName()});
		}
		
		return null;
	}
}
