package net.kardexo.kardexotools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;

import net.kardexo.kardexotools.command.CommandBackup;
import net.kardexo.kardexotools.command.CommandBases;
import net.kardexo.kardexotools.command.CommandCalculate;
import net.kardexo.kardexotools.command.CommandHome;
import net.kardexo.kardexotools.command.CommandKardExo;
import net.kardexo.kardexotools.command.CommandMoonPhase;
import net.kardexo.kardexotools.command.CommandPlaces;
import net.kardexo.kardexotools.command.CommandResource;
import net.kardexo.kardexotools.command.CommandSetBiome;
import net.kardexo.kardexotools.command.CommandSetHome;
import net.kardexo.kardexotools.command.CommandSpawn;
import net.kardexo.kardexotools.command.CommandUndo;
import net.kardexo.kardexotools.command.CommandVeinminer;
import net.kardexo.kardexotools.command.CommandWhereIs;
import net.kardexo.kardexotools.command.CommandWorldTime;
import net.kardexo.kardexotools.config.AbstractConfigFile;
import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.config.ConfigFile;
import net.kardexo.kardexotools.config.DataFile;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.VeinBlockConfig;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.tasks.TaskBackup;
import net.kardexo.kardexotools.tasks.TaskSave;
import net.kardexo.kardexotools.tasks.TaskScheduler;
import net.kardexo.kardexotools.tasks.TickableBases;
import net.kardexo.kardexotools.tasks.TickableDeathListener;
import net.kardexo.kardexotools.tasks.TickableSleep;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class KardExo
{
	public static final String VERSION = "1.16.5-2.42";
	public static final Logger LOGGER = LogManager.getLogger("KardExo");
	
	private static final File CONFIG_DIRECTORY = new File("config");
	private static final TaskScheduler TASK_SCHEDULER = new TaskScheduler();
	
	public static final ConfigFile<Config> CONFIG_FILE = new ConfigFile<Config>(new File(CONFIG_DIRECTORY, "config.json"), Config.class, Config::new);
	public static final DataFile<Property, String> BASES_FILE = new DataFile<Property, String>(new File(CONFIG_DIRECTORY, "bases.json"), Property[].class, Property::getName);
	public static final DataFile<Property, String> PLACES_FILE = new DataFile<Property, String>(new File(CONFIG_DIRECTORY, "places.json"), Property[].class, Property::getName);
	public static final DataFile<PlayerConfig, String> PLAYERS_FILE = new DataFile<PlayerConfig, String>(new File(CONFIG_DIRECTORY, "playerdata.json"), PlayerConfig[].class, PlayerConfig::getPlayer);
	public static final DataFile<VeinBlockConfig, Block> VEINMINER_FILE = new DataFile<VeinBlockConfig, Block>(new File(CONFIG_DIRECTORY, "veinminer.json"), VeinBlockConfig[].class, VeinBlockConfig::toBlock, KardExo::defaultVeinminerConfig);
	
	public static final Config CONFIG = CONFIG_FILE.getData();
	public static final Map<String, Property> BASES = BASES_FILE.getMap();
	public static final Map<String, Property> PLACES = PLACES_FILE.getMap();
	public static final Map<String, PlayerConfig> PLAYERS = PLAYERS_FILE.getMap();
	public static final Map<Block, VeinBlockConfig> VEINMINER = VEINMINER_FILE.getMap();
	
	private static final List<AbstractConfigFile<?>> CONFIG_FILES = Lists.newArrayList(CONFIG_FILE, BASES_FILE, PLACES_FILE, PLAYERS_FILE, VEINMINER_FILE);
	
	public static void init(MinecraftServer server)
	{
		LOGGER.info("Loading KardExoTools " + KardExo.VERSION);
		KardExo.setupLevelSaving(server);
		KardExo.createConfigDirectory();
		KardExo.readAllFiles();
		KardExo.registerTickables(server);
		KardExo.registerCommands(server.getCommands().getDispatcher());
		KardExo.registerTasks(server, CONFIG, TASK_SCHEDULER);
	}
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		CommandMoonPhase.register(dispatcher);
		CommandWhereIs.register(dispatcher);
		CommandBackup.register(dispatcher);
		CommandWorldTime.register(dispatcher);
		CommandBases.register(dispatcher);
		CommandResource.register(dispatcher);
		CommandCalculate.register(dispatcher);
		CommandPlaces.register(dispatcher);
		CommandHome.register(dispatcher);
		CommandSetHome.register(dispatcher);
		CommandSpawn.register(dispatcher);
		CommandVeinminer.register(dispatcher);
		CommandUndo.register(dispatcher);
		CommandKardExo.register(dispatcher);
		CommandSetBiome.register(dispatcher);
	}
	
	public static void registerTasks(MinecraftServer server, Config config, TaskScheduler scheduler)
	{
		scheduler.schedule(new TaskSave(server), config.getSaveOffset(), TimeUnit.MINUTES, config.getSaveInterval(), TimeUnit.MINUTES, config.getSaveWarningTimes(), TimeUnit.SECONDS);
		scheduler.schedule(new TaskBackup(server), config.getBackupOffset(), TimeUnit.MINUTES, config.getBackupInterval(), TimeUnit.MINUTES, config.getBackupWarningTimes(), TimeUnit.SECONDS);
	}
	
	public static void registerTickables(MinecraftServer server)
	{
		server.addTickable(new TickableSleep(server));
		server.addTickable(new TickableBases(server));
		server.addTickable(new TickableDeathListener(server));
	}
	
	private static void setupLevelSaving(MinecraftServer server)
	{
		if(CONFIG.doDisableAutoSaving())
		{
			for(ServerLevel worldserver : server.getAllLevels())
			{
				if(worldserver != null && !worldserver.noSave)
				{
					worldserver.noSave = true;
				}
			}
		}
	}
	
	public static void notifyPlayers(MinecraftServer server, Component message)
	{
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().broadcastMessage(message, ChatType.SYSTEM, Util.NIL_UUID);
		}
	}
	
	public static synchronized void saveLevels(MinecraftServer server)
	{
		KardExo.saveLevels(server, true);
	}
	
	public static synchronized void saveLevels(MinecraftServer server, boolean displayMessages)
	{
		if(displayMessages)
		{
			KardExo.notifyPlayers(server, new TranslatableComponent("commands.save.saving", new Object[0]));
		}
		
		if(server.getPlayerList() != null)
		{
			server.getPlayerList().saveAll();
		}
		
		if(server.saveAllChunks(true, true, false) && displayMessages)
		{
			KardExo.notifyPlayers(server, new TranslatableComponent("commands.save.success", new Object[0]));
		}
		else if(displayMessages)
		{
			KardExo.notifyPlayers(server, new TranslatableComponent("commands.save.failed"));
		}
	}
	
	public static void stop()
	{
		TASK_SCHEDULER.stop();
		KardExo.saveAllFiles();
	}
	
	public static void saveAllFiles()
	{
		CONFIG_FILES.forEach(AbstractConfigFile::save);
	}
	
	public static void readAllFiles()
	{
		CONFIG_FILES.forEach(AbstractConfigFile::read);
	}
	
	public static void createConfigDirectory()
	{
		try
		{
			Files.createDirectories(CONFIG_DIRECTORY.toPath());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static List<VeinBlockConfig> defaultVeinminerConfig()
	{
		List<VeinBlockConfig> config = new ArrayList<VeinBlockConfig>();
		
		config.add(new VeinBlockConfig(Blocks.OAK_LOG, 12, true));
		config.add(new VeinBlockConfig(Blocks.SPRUCE_LOG, 26, true));
		config.add(new VeinBlockConfig(Blocks.JUNGLE_LOG, 26, true));
		config.add(new VeinBlockConfig(Blocks.BIRCH_LOG, 15, true));
		config.add(new VeinBlockConfig(Blocks.DARK_OAK_LOG, 10, true));
		config.add(new VeinBlockConfig(Blocks.ACACIA_LOG, 10, true));
		config.add(new VeinBlockConfig(Blocks.CRIMSON_STEM, 26, true));
		config.add(new VeinBlockConfig(Blocks.WARPED_STEM, 26, true));
		
		config.add(new VeinBlockConfig(Blocks.OAK_LEAVES, 5, true));
		config.add(new VeinBlockConfig(Blocks.SPRUCE_LEAVES, 5, true));
		config.add(new VeinBlockConfig(Blocks.JUNGLE_LEAVES, 5, true));
		config.add(new VeinBlockConfig(Blocks.BIRCH_LEAVES, 5, true));
		config.add(new VeinBlockConfig(Blocks.DARK_OAK_LEAVES, 5, true));
		config.add(new VeinBlockConfig(Blocks.ACACIA_LEAVES, 5, true));
		config.add(new VeinBlockConfig(Blocks.NETHER_WART_BLOCK, 5, true));
		config.add(new VeinBlockConfig(Blocks.WARPED_WART_BLOCK, 5, true));
		
		config.add(new VeinBlockConfig(Blocks.ANDESITE, 15, true));
		config.add(new VeinBlockConfig(Blocks.DIORITE, 15, true));
		config.add(new VeinBlockConfig(Blocks.GRANITE, 15, true));
		
		config.add(new VeinBlockConfig(Blocks.GRAVEL, 6, true));
		config.add(new VeinBlockConfig(Blocks.GLOWSTONE, 10, false));
		config.add(new VeinBlockConfig(Blocks.SOUL_SAND, 5, true));
		config.add(new VeinBlockConfig(Blocks.OBSIDIAN, 5, true));
		config.add(new VeinBlockConfig(Blocks.CRYING_OBSIDIAN, 5, true));
		config.add(new VeinBlockConfig(Blocks.SAND, 7, true));
		config.add(new VeinBlockConfig(Blocks.RED_SAND, 5, true));
		config.add(new VeinBlockConfig(Blocks.CLAY, 4, true));
		
		config.add(new VeinBlockConfig(Blocks.COAL_ORE, 17, true));
		config.add(new VeinBlockConfig(Blocks.IRON_ORE, 9, true));
		config.add(new VeinBlockConfig(Blocks.GOLD_ORE, 9, true));
		config.add(new VeinBlockConfig(Blocks.GILDED_BLACKSTONE, 9, true));
		config.add(new VeinBlockConfig(Blocks.DIAMOND_ORE, 9, true));
		config.add(new VeinBlockConfig(Blocks.LAPIS_ORE, 7, true));
		config.add(new VeinBlockConfig(Blocks.REDSTONE_ORE, 8, true));
		config.add(new VeinBlockConfig(Blocks.NETHER_QUARTZ_ORE, 14, true));
		config.add(new VeinBlockConfig(Blocks.NETHER_GOLD_ORE, 10, true));
		config.add(new VeinBlockConfig(Blocks.ANCIENT_DEBRIS, 3, true));
		
		config.add(new VeinBlockConfig(Blocks.ICE, 10, true));
		config.add(new VeinBlockConfig(Blocks.PACKED_ICE, 10, true));
		config.add(new VeinBlockConfig(Blocks.BLUE_ICE, 10, true));
		config.add(new VeinBlockConfig(Blocks.BONE_BLOCK, 10, true));
		config.add(new VeinBlockConfig(Blocks.WET_SPONGE, 5, true));
		
		return config;
	}
}
