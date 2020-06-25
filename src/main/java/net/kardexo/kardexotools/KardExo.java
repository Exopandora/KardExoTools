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
		KardExo.registerCommands(server.getCommandManager().getDispatcher());
		
		KardExo.TASK_SCHEDULER.schedule(new TaskSave(server), TimeUnit.MINUTES.toMillis(Config.OFFSET_SAVE), TimeUnit.MINUTES.toMillis(Config.INTERVAL_SAVE), Config.WARNING_TIMES_SAVE);
		KardExo.TASK_SCHEDULER.schedule(new TaskBackup(server), TimeUnit.MINUTES.toMillis(Config.OFFSET_BACKUP), TimeUnit.MINUTES.toMillis(Config.INTERVAL_BACKUP), Config.WARNING_TIMES_BACKUP);
	}
	
	private static void setupLevelSaving(MinecraftServer server)
	{
		if(Config.DISABLE_AUTO_SAVING)
		{
			for(ServerWorld worldserver : server.getWorlds())
			{
				if(worldserver != null && !worldserver.disableLevelSaving)
				{
					worldserver.disableLevelSaving = true;
				}
			}
		}
	}
	
	public static void registerTickables(MinecraftServer server)
	{
		server.registerTickable(new TickableSleep(server));
		server.registerTickable(new TickableBases(server));
		server.registerTickable(new TickableDeathListener(server));
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
			server.getPlayerList().func_232641_a_(message, ChatType.SYSTEM, Util.field_240973_b_);
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
			server.getPlayerList().saveAllPlayerData();
		}
		
		if(server.save(true, true, false))
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
