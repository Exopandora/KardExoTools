package net.kardexo.kardexotools.util;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.time.Duration;
import java.util.Locale;

public class Util
{
	public static synchronized void saveLevels(MinecraftServer server, boolean displayMessages)
	{
		if(displayMessages)
		{
			broadcastMessage(server, Component.translatable("commands.save.saving"));
		}
		
		boolean success = server.saveEverything(true, KardExo.CONFIG.getData().isSaveFlush(), true);
		
		if(displayMessages)
		{
			if(success)
			{
				broadcastMessage(server, Component.translatable("commands.save.success"));
			}
			else
			{
				broadcastMessage(server, Component.translatable("commands.save.failed"));
			}
		}
	}
	
	public static synchronized void saveLevels(MinecraftServer server)
	{
		saveLevels(server, true);
	}
	
	public static void broadcastMessage(MinecraftServer server, Component message)
	{
		server.getPlayerList().broadcastSystemMessage(message, false);
	}
	
	public static String format(Duration duration)
	{
		long seconds = duration.toSeconds();
		long minutes = seconds / 60;
		seconds %= 60;
		long hours = minutes / 60;
		minutes %= 60;
		long days = hours / 24;
		hours %= 24;
		
		if(days > 0)
		{
			return String.format(Locale.ROOT, "%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
		}
		else if(hours > 0)
		{
			return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
		}
		
		return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
	}
}
