package exopandora.kardexo.kardexotools;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;

import exopandora.kardexo.kardexotools.command.arguments.BiomeArgument;
import exopandora.kardexo.kardexotools.data.Config;
import exopandora.kardexo.kardexotools.tasks.ShutdownHook;
import exopandora.kardexo.kardexotools.tasks.Tasks;
import exopandora.kardexo.kardexotools.tasks.TickableBases;
import exopandora.kardexo.kardexotools.tasks.TickableDeathListener;
import exopandora.kardexo.kardexotools.tasks.TickableSleep;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SessionLockException;

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
			for(WorldServer worldserver : server.getWorlds())
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
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
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
		registerArgumentType(new ResourceLocation("kardexo:biome"), BiomeArgument::biome, BiomeArgument.class);
	}
	
	private static <T extends ArgumentType<?>> void registerArgumentType(ResourceLocation location, Supplier<T> supplier, Class<T> klass)
	{
		ArgumentTypes.register(location, klass, new ArgumentSerializer<T>(supplier));
		SuggestionProviders.register(location, supplier.get()::listSuggestions);
	}
	
	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher)
	{
		Config.getCommands().forEach(command -> command.accept(dispatcher));
	}
	
	public static void notifyPlayers(MinecraftServer server, ITextComponent message)
	{
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().sendMessage(message);
		}
	}
	
	public static synchronized void saveWorld()
	{
		KardExo.saveWorld(true);
	}
	
	public static synchronized void saveWorld(boolean displayMessages)
	{
		MinecraftServer server = KardExo.getServer();
		
		if(displayMessages)
		{
			KardExo.notifyPlayers(server, new TextComponentTranslation("commands.save.saving", new Object[0]));
		}
		
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().saveAllPlayerData();;
		}
		
		try
		{
			for(WorldServer worldserver : server.getWorlds())
			{
				if(worldserver != null)
				{
					boolean flag = worldserver.disableLevelSaving;
					worldserver.disableLevelSaving = false;
					worldserver.saveAllChunks(true, (IProgressUpdate) null);
					worldserver.flushToDisk();
					worldserver.disableLevelSaving = flag;
				}
			}
		}
		catch(SessionLockException e)
		{
			if(displayMessages)
			{
				KardExo.notifyPlayers(server, new TextComponentTranslation("commands.save.failed", new Object[]{e.getMessage()}));
			}
			
			e.printStackTrace();
			
			return;
		}
		
		if(displayMessages)
		{
			KardExo.notifyPlayers(server, new TextComponentTranslation("commands.save.success", new Object[0]));
		}
	}
	
	public static MinecraftServer getServer()
	{
		return KardExo.SERVER;
	}
}
