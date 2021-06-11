package net.kardexo.kardexotools.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.property.BaseAccess;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.property.PropertyOwner;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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
		for(Property base : KardExo.BASES.values())
		{
			List<PropertyOwner> notifyList = new ArrayList<PropertyOwner>();
			
			for(PropertyOwner owner : base.getAllOwners())
			{
				if(owner.doNotify())
				{
					notifyList.add(owner);
				}
			}
			
			for(ServerPlayer player : this.server.getPlayerList().getPlayers())
			{
				Set<String> visitors = BASE_VISITORS.computeIfAbsent(base, key -> new HashSet<String>());
				String name = player.getGameProfile().getName();
				
				boolean inside = base.isInside(player);
				boolean contains = visitors.contains(name);
				
				if(!inside && contains)
				{
					visitors.remove(name);
					this.notifyOwners(base, notifyList, player, BaseAccess.LEAVE);
				}
				else if(inside && !contains)
				{
					visitors.add(name);
					this.notifyOwners(base, notifyList, player, BaseAccess.ENTER);
				}
			}
		}
	}
	
	private void notifyOwners(Property base, List<PropertyOwner> notify, ServerPlayer player, BaseAccess access)
	{
		String name = player.getGameProfile().getName();
		
		if(!base.isOwner(name))
		{
			switch(access)
			{
				case ENTER:
					this.server.sendMessage(new TextComponent(name + " has entered base with id " + base.getTitle()), null);;
					break;
				case LEAVE:
					this.server.sendMessage(new TextComponent(name + " has left base with id " + base.getTitle()), null);
					break;
			}
			
			for(PropertyOwner owner : notify)
			{
				ServerPlayer playerOwner = this.server.getPlayerList().getPlayerByName(owner.getName());
				
				if(playerOwner != null)
				{
					playerOwner.sendMessage(this.getFormattedMessage(player, base, owner, access), Util.NIL_UUID);
				}
			}
		}
	}
	
	private Component getFormattedMessage(ServerPlayer player, Property base, PropertyOwner owner, BaseAccess access)
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
			return new TranslatableComponent(format.replace("&name", "%1$s").replace("&base", "%2$s"), new Object[]{player.getDisplayName(), base.getDisplayName()});
		}
		
		switch(access)
		{
			case ENTER:
				return new TranslatableComponent(KardExo.CONFIG.getPropertyDefaultEnterMessage(), new Object[]{player.getDisplayName(), base.getDisplayName()});
			case LEAVE:
				return new TranslatableComponent(KardExo.CONFIG.getPropertyDefaultExitMessage(), new Object[]{player.getDisplayName(), base.getDisplayName()});
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
