package net.kardexo.kardexotools.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.OwnerConfig;
import net.kardexo.kardexotools.property.BaseAccess;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class BasesTickable implements Runnable
{
	private static final Map<Property, Set<String>> BASE_VISITORS = new HashMap<Property, Set<String>>();
	
	private final MinecraftServer server;
	
	public BasesTickable(MinecraftServer server)
	{
		this.server = server;
	}
	
	@Override
	public void run()
	{
		if(this.server.getPlayerList().getPlayers().isEmpty())
		{
			return;
		}
		
		for(Entry<String, Property> entry : KardExo.BASES.getData().entrySet())
		{
			Property base = entry.getValue();
			
			if(base.getOwners() == null)
			{
				continue;
			}
			
			Map<UUID, OwnerConfig> notifyList = new HashMap<UUID, OwnerConfig>();
			
			for(Entry<UUID, OwnerConfig> owner : base.getOwners().entrySet())
			{
				if(owner.getValue().doNotify())
				{
					notifyList.put(owner.getKey(), owner.getValue());
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
					this.notifyOwners(entry.getKey(), base, notifyList, player, BaseAccess.LEAVE);
				}
				else if(inside && !contains)
				{
					visitors.add(name);
					this.notifyOwners(entry.getKey(), base, notifyList, player, BaseAccess.ENTER);
				}
			}
		}
	}
	
	private void notifyOwners(String id, Property base, Map<UUID, OwnerConfig> notify, ServerPlayer player, BaseAccess access)
	{
		if(!base.isOwner(player.getGameProfile().getId()))
		{
			switch(access)
			{
				case ENTER:
					this.server.sendSystemMessage(((MutableComponent) player.getDisplayName()).append(" has entered base with id " + base.getDisplayName(id)));
					break;
				case LEAVE:
					this.server.sendSystemMessage(((MutableComponent) player.getDisplayName()).append(" has left base with id " + base.getDisplayName(id)));
					break;
			}
			
			for(Entry<UUID, OwnerConfig> entry : notify.entrySet())
			{
				ServerPlayer playerOwner = this.server.getPlayerList().getPlayer(entry.getKey());
				
				if(playerOwner != null)
				{
					playerOwner.sendSystemMessage(this.getFormattedMessage(id, player, base, entry.getValue(), access));
				}
			}
		}
	}
	
	private Component getFormattedMessage(String id, ServerPlayer player, Property base, OwnerConfig owner, BaseAccess access)
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
			return Component.translatable(format.replace("&name", "%1$s").replace("&base", "%2$s"), new Object[]{player.getDisplayName(), base.getDisplayName(id, player.getServer().getProfileCache())});
		}
		
		switch(access)
		{
			case ENTER:
				return Component.translatable(KardExo.CONFIG.getData().getPropertyDefaultEnterMessage(), new Object[]{player.getDisplayName(), base.getDisplayName(id, player.getServer().getProfileCache())});
			case LEAVE:
				return Component.translatable(KardExo.CONFIG.getData().getPropertyDefaultExitMessage(), new Object[]{player.getDisplayName(), base.getDisplayName(id, player.getServer().getProfileCache())});
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
