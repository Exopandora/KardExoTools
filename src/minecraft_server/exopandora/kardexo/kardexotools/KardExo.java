package exopandora.kardexo.kardexotools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		
		for(int i = 0; i < server.worldServers.length; i++)
	    {
	        if(server.worldServers[i] != null)
	        {
	            WorldServer worldserver = server.worldServers[i];
	            
	            if(!worldserver.disableLevelSaving)
	            {
	                worldserver.disableLevelSaving = true;
	            }
	        }
	    }
		
		KardExo.LOGGER.info("Loading files");
		
		DataFile.readAllFiles();
		
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
			server.getServer().getPlayerList().sendChatMsg(message);
		}
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
			for(int dimension = 0; dimension < KardExo.getServer().worldServers.length; dimension++)
			{
				if(KardExo.getServer().worldServers[dimension] != null)
				{
					WorldServer worldserver = KardExo.getServer().worldServers[dimension];
					boolean flag = worldserver.disableLevelSaving;
					worldserver.disableLevelSaving = false;
                    worldserver.saveAllChunks(true, (IProgressUpdate)null);
					worldserver.saveChunkData();
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
