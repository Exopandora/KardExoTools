package net.kardexo.kardexotools.util;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;

public class Util
{
	public static synchronized void saveLevels(MinecraftServer server, boolean displayMessages)
	{
		if(displayMessages)
		{
			broadcastMessage(server, new TranslatableComponent("commands.save.saving", new Object[0]));
		}
		
		boolean success = server.saveEverything(true, KardExo.CONFIG.getData().isSaveFlush(), false);
		
		if(displayMessages)
		{
			if(success)
			{
				broadcastMessage(server, new TranslatableComponent("commands.save.success"));
			}
			else
			{
				broadcastMessage(server, new TranslatableComponent("commands.save.failed"));
			}
		}
	}
	
	public static synchronized void saveLevels(MinecraftServer server)
	{
		saveLevels(server, true);
	}
	
	public static void broadcastMessage(MinecraftServer server, Component message)
	{
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().broadcastMessage(message, ChatType.SYSTEM, net.minecraft.Util.NIL_UUID);
		}
	}
}
