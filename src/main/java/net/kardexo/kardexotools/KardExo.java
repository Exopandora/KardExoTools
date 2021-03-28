package net.kardexo.kardexotools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.tasks.TaskBackup;
import net.kardexo.kardexotools.tasks.TaskSave;
import net.kardexo.kardexotools.tasks.TaskScheduler;
import net.kardexo.kardexotools.tasks.TickableBases;
import net.kardexo.kardexotools.tasks.TickableDeathListener;
import net.kardexo.kardexotools.tasks.TickableSleep;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class KardExo
{
	public static final Logger LOGGER = LogManager.getLogger("KardExo");
	public static final TaskScheduler TASK_SCHEDULER = new TaskScheduler();
	
	public static void init(MinecraftServer server)
	{
		KardExo.LOGGER.info("Loading KardExoTools " + Config.VERSION);
		KardExo.setupLevelSaving(server);
		
		try
		{
			Config.readAllFiles();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		KardExo.registerTickables(server);
		KardExo.registerCommands(server.getCommands().getDispatcher());
		
		KardExo.TASK_SCHEDULER.schedule(new TaskSave(server), Config.OFFSET_SAVE, TimeUnit.MINUTES, Config.INTERVAL_SAVE, TimeUnit.MINUTES, Config.WARNING_TIMES_SAVE, TimeUnit.SECONDS);
		KardExo.TASK_SCHEDULER.schedule(new TaskBackup(server), Config.OFFSET_BACKUP, TimeUnit.MINUTES, Config.INTERVAL_BACKUP, TimeUnit.MINUTES, Config.WARNING_TIMES_BACKUP, TimeUnit.SECONDS);
	}
	
	private static void setupLevelSaving(MinecraftServer server)
	{
		if(Config.DISABLE_AUTO_SAVING)
		{
			for(ServerWorld worldserver : server.getAllLevels())
			{
				if(worldserver != null && !worldserver.noSave)
				{
					worldserver.noSave = true;
				}
			}
		}
	}
	
	public static void registerTickables(MinecraftServer server)
	{
		server.addTickable(new TickableSleep(server));
		server.addTickable(new TickableBases(server));
		server.addTickable(new TickableDeathListener(server));
	}
	
	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher)
	{
		List<Consumer<CommandDispatcher<CommandSource>>> commands = new ArrayList<Consumer<CommandDispatcher<CommandSource>>>();
		Config.commands(commands);
		commands.forEach(command -> command.accept(dispatcher));
	}
	
	public static void notifyPlayers(MinecraftServer server, ITextComponent message)
	{
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().broadcastMessage(message, ChatType.SYSTEM, Util.NIL_UUID);
		}
	}
	
	public static synchronized void saveWorlds(MinecraftServer server)
	{
		KardExo.saveWorlds(server, true);
	}
	
	public static synchronized void saveWorlds(MinecraftServer server, boolean displayMessages)
	{
		if(displayMessages)
		{
			KardExo.notifyPlayers(server, new TranslationTextComponent("commands.save.saving", new Object[0]));
		}
		
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().saveAll();
		}
		
		if(server.saveAllChunks(true, true, false))
		{
			if(displayMessages)
			{
				KardExo.notifyPlayers(server, new TranslationTextComponent("commands.save.success", new Object[0]));
			}
		}
		else
		{
			if(displayMessages)
			{
				KardExo.notifyPlayers(server, new TranslationTextComponent("commands.save.failed"));
			}
		}
	}
	
	public static void stop()
	{
		KardExo.TASK_SCHEDULER.stop();
		Config.saveAllFiles();
	}
}
