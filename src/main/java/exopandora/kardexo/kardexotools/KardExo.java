package exopandora.kardexo.kardexotools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;

import exopandora.kardexo.kardexotools.command.arguments.BiomeArgument;
import exopandora.kardexo.kardexotools.config.Config;
import exopandora.kardexo.kardexotools.tasks.Tasks;
import exopandora.kardexo.kardexotools.tasks.TickableBases;
import exopandora.kardexo.kardexotools.tasks.TickableDeathListener;
import exopandora.kardexo.kardexotools.tasks.TickableSleep;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class KardExo
{
	public static final Logger LOGGER = LogManager.getLogger("KardExo");
	
	private static MinecraftServer SERVER;
	
	public static void init(MinecraftServer server)
	{
		KardExo.LOGGER.info("Loading KardExoTools " + Config.VERSION);
		
		SERVER = server;
		
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
		
		try
		{
			Config.readAllFiles();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		KardExo.registerTickables(server);
		KardExo.registerArguments();
		KardExo.registerCommands(server.getCommandManager().getDispatcher());
		
		Tasks.start();
	}
	
	public static void registerTickables(MinecraftServer server)
	{
		server.registerTickable(new TickableSleep(server));
		server.registerTickable(new TickableBases(server));
		server.registerTickable(new TickableDeathListener(server));
	}
	
	private static void registerArguments()
	{
		ArgumentTypes.register("biome", BiomeArgument.class, new ArgumentSerializer<BiomeArgument>(BiomeArgument::biome));
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
			server.getPlayerList().sendMessage(message);
		}
	}
	
	public static synchronized void saveWorlds()
	{
		KardExo.saveWorlds(true);
	}
	
	public static synchronized void saveWorlds(boolean displayMessages)
	{
		MinecraftServer server = KardExo.getServer();
		
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
		Tasks.stop();
		Config.saveAllFiles();
	}
	
	public static MinecraftServer getServer()
	{
		return KardExo.SERVER;
	}
}
