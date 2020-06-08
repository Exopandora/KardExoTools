package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.property.EnumBaseAccess;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.property.PropertyOwner;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TickableBases implements Runnable
{
	private static final Map<Property, Set<String>> BASE_VISITORS = new HashMap<Property, Set<String>>();
	
	private final MinecraftServer server;
	
	public TickableBases(MinecraftServer dedicatedserver)
	{
		this.server = dedicatedserver;
	}
	
	@Override
	public void run()
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
			
			for(ServerPlayerEntity player : this.server.getPlayerList().getPlayers())
			{
				Set<String> visitors = BASE_VISITORS.computeIfAbsent(base, key -> new HashSet<String>());
				
				boolean inside = base.isInside(player);
				boolean contains = visitors.contains(player.getName().getString());
				
				if(!inside && contains)
				{
					visitors.remove(player.getName().getString());
					this.notifyOwners(base, notifyList, player, EnumBaseAccess.LEAVE);
				}
				else if(inside && !contains)
				{
					visitors.add(player.getName().getString());
					this.notifyOwners(base, notifyList, player, EnumBaseAccess.ENTER);
				}
			}
		}
	}
	
	private void notifyOwners(Property base, List<PropertyOwner> notify, ServerPlayerEntity player, EnumBaseAccess access)
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
				ServerPlayerEntity playerOwner = this.server.getPlayerList().getPlayerByUsername(owner.getName());
				
				if(playerOwner != null)
				{
					playerOwner.sendMessage(this.getFormattedMessage(player, base, owner, access));
				}
			}
		}
	}
	
	private ITextComponent getFormattedMessage(ServerPlayerEntity player, Property base, PropertyOwner owner, EnumBaseAccess access)
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
			return new TranslationTextComponent(format.replace("&name", "%1$s").replace("&base", "%2$s"), new Object[]{player.getDisplayName(), base.getDisplayName()});
		}
		
		switch(access)
		{
			case ENTER:
				return new TranslationTextComponent(PropertyOwner.getDefaultEnterMessage(), new Object[]{player.getDisplayName(), base.getDisplayName()});
			case LEAVE:
				return new TranslationTextComponent(PropertyOwner.getDefaultExitMessage(), new Object[]{player.getDisplayName(), base.getDisplayName()});
		}
		
		return null;
	}
	
	public static void remove(Property base)
	{
		BASE_VISITORS.remove(base);
	}
	
	public static Set<String> get(Property base)
	{
		return BASE_VISITORS.get(base);
	}
	
	public static void reload()
	{
		BASE_VISITORS.clear();
	}
}
