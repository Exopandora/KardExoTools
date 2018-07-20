package exopandora.kardexo.kardexotools;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

public class KardExo
{
	public static final Logger LOGGER = LogManager.getLogger("KardExo");
	
	private static MinecraftServer SERVER;
	
	public static void init(MinecraftServer server)
	{
		KardExo.LOGGER.info("KardExoTools " + Config.VERSION + " bootstrap");
		
		SERVER = server;
		
		for(int i = 0; i < server.worlds.length; i++)
		{
			if(server.worlds[i] != null)
			{
				WorldServer worldserver = server.worlds[i];
				
				if(!worldserver.disableLevelSaving)
				{
					worldserver.disableLevelSaving = true;
				}
			}
		}
		
		KardExo.LOGGER.info("Loading files");
		
		Config.readAllFiles();
		
		KardExo.LOGGER.info("Adding hooks");
		
		server.registerTickable(new TickableSleep(server));
		server.registerTickable(new TickableBases(server));
		server.registerTickable(new TickableDeathListener(server));
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
		try
		{
			Tasks.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		KardExo.LOGGER.info("Turned off world auto-saving");
	}
	
	public static void notifyPlayers(MinecraftServer server, ITextComponent message)
	{
		if(server.getServer().getPlayerList() != null)
		{
			server.getServer().getPlayerList().sendMessage(message);
		}
	}
	
	public static synchronized void saveWorld()
	{
		KardExo.saveWorld(true);
	}
	
	public static synchronized void saveWorld(boolean displayMessages)
	{
		if(displayMessages)
		{
			KardExo.notifyPlayers(KardExo.getServer(), new TextComponentTranslation("commands.save.start", new Object[0]));
		}
		
		if(KardExo.getServer().getPlayerList() != null)
		{
			KardExo.getServer().getPlayerList().saveAllPlayerData();
		}
		
		try
		{
			for(int dimension = 0; dimension < KardExo.getServer().worlds.length; dimension++)
			{
				if(KardExo.getServer().worlds[dimension] != null)
				{
					WorldServer worldserver = KardExo.getServer().worlds[dimension];
					boolean flag = worldserver.disableLevelSaving;
					worldserver.disableLevelSaving = false;
					worldserver.saveAllChunks(true, (IProgressUpdate)null);
					worldserver.flushToDisk();
					worldserver.flush();
					worldserver.disableLevelSaving = flag;
				}
			}
		}
		catch(Exception e)
		{
			if(displayMessages)
			{
				KardExo.notifyPlayers(KardExo.getServer(), new TextComponentTranslation("commands.save.failed", new Object[]{e.getMessage()}));
			}
			
			e.printStackTrace();
			
			return;
		}
		
		if(displayMessages)
		{
			KardExo.notifyPlayers(KardExo.getServer(), new TextComponentTranslation("commands.save.success", new Object[0]));
		}
	}
	
	public static MinecraftServer getServer()
	{
		return KardExo.SERVER;
	}
	
	public static final List<ICommand> getCommands()
	{
		List<ICommand> commands = new ArrayList<ICommand>();
		
		commands.add(new CommandMoonPhase());
		commands.add(new CommandWhereIs());
		commands.add(new CommandBackup());
		commands.add(new CommandWorldTime());
		commands.add(new CommandBases());
		commands.add(new CommandResource());
		commands.add(new CommandForceSave());
		commands.add(new CommandCalculate());
		commands.add(new CommandPlaces());
		commands.add(new CommandHome());
		commands.add(new CommandSetHome());
		commands.add(new CommandSpawn());
		commands.add(new CommandVeinminer());
		commands.add(new CommandUndo());
		commands.add(new CommandLocateBiome());
		commands.add(new CommandKardExo());
		
		return commands;
	}
}
