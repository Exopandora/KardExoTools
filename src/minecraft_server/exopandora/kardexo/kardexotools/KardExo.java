package exopandora.kardexo.kardexotools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.math.BlockPos;
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
		
		Tasks.start();
		
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
}