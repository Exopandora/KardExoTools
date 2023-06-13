package net.kardexo.kardexotools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;

import net.kardexo.kardexotools.command.BackupCommand;
import net.kardexo.kardexotools.command.BasesCommand;
import net.kardexo.kardexotools.command.CalculateCommand;
import net.kardexo.kardexotools.command.HomeCommand;
import net.kardexo.kardexotools.command.KardExoCommand;
import net.kardexo.kardexotools.command.MoonPhaseCommand;
import net.kardexo.kardexotools.command.PackCommand;
import net.kardexo.kardexotools.command.PlacesCommand;
import net.kardexo.kardexotools.command.ResourceCommand;
import net.kardexo.kardexotools.command.SetHomeCommand;
import net.kardexo.kardexotools.command.SpawnCommand;
import net.kardexo.kardexotools.command.UndoCommand;
import net.kardexo.kardexotools.command.UptimeCommand;
import net.kardexo.kardexotools.command.VeinminerCommand;
import net.kardexo.kardexotools.command.WhereIsCommand;
import net.kardexo.kardexotools.command.WorldTimeCommand;
import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.config.ConfigFile;
import net.kardexo.kardexotools.config.MapFile;
import net.kardexo.kardexotools.config.PlayerConfig;
import net.kardexo.kardexotools.config.VeinConfig;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.tasks.BackupTask;
import net.kardexo.kardexotools.tasks.BasesTickable;
import net.kardexo.kardexotools.tasks.ITask;
import net.kardexo.kardexotools.tasks.SaveTask;
import net.kardexo.kardexotools.tasks.ShutdownTask;
import net.kardexo.kardexotools.tasks.TaskDispatcher;
import net.kardexo.kardexotools.tasks.TaskScheduler;
import net.kardexo.kardexotools.util.BlockPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class KardExo
{
	public static final String VERSION = "1.20-2.51.1";
	public static final Logger LOGGER = LogManager.getLogger("KardExo");
	
	private static final File CONFIG_DIRECTORY = new File("config/kardexotools");
	
	public static final ConfigFile<Config> CONFIG = new ConfigFile<Config>(new File(CONFIG_DIRECTORY, "config.json"), new TypeToken<Config>() {});
	public static final MapFile<String, Property> BASES = new MapFile<String, Property>(new File(CONFIG_DIRECTORY, "bases.json"), new TypeToken<Map<String, Property>>() {});
	public static final MapFile<String, Property> PLACES = new MapFile<String, Property>(new File(CONFIG_DIRECTORY, "places.json"), new TypeToken<Map<String, Property>>() {});
	public static final MapFile<UUID, PlayerConfig> PLAYERS = new MapFile<UUID, PlayerConfig>(new File(CONFIG_DIRECTORY, "playerdata.json"), new TypeToken<Map<UUID, PlayerConfig>>() {});
	public static final MapFile<BlockPredicate, VeinConfig> VEINMINER = new MapFile<BlockPredicate, VeinConfig>(new File(CONFIG_DIRECTORY, "veinminer.json"), new TypeToken<Map<BlockPredicate, VeinConfig>>() {});
	
	private static final List<ConfigFile<?>> CONFIG_FILES = Lists.newArrayList(CONFIG, BASES, PLACES, PLAYERS, VEINMINER);
	
	public static final ITask TASK_SAVE = new SaveTask();
	public static final ITask TASK_BACKUP = new BackupTask();
	public static final ITask TASK_SHUTDOWN = new ShutdownTask();
	
	public static void preInit(MinecraftServer server)
	{
		LOGGER.info("KardExoTools " + KardExo.VERSION);
		createConfigDirectory();
		initConfigs(server);
		readConfigs();
		setLevelSaving(server, !CONFIG.getData().isDisableAutoSaving());
		registerTickables(server);
		registerCommands(server.getCommands().getDispatcher());
	}
	
	public static void postInit(MinecraftServer server)
	{
		LOGGER.info("Starting tasks");
		TaskScheduler scheduler = new TaskScheduler(new TaskDispatcher(server));
		scheduler.registerTask(TASK_SAVE);
		scheduler.registerTask(TASK_BACKUP);
		scheduler.registerTask(TASK_SHUTDOWN);
		scheduler.setDaemon(true);
		scheduler.start();
	}
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		MoonPhaseCommand.register(dispatcher);
		WhereIsCommand.register(dispatcher);
		BackupCommand.register(dispatcher);
		WorldTimeCommand.register(dispatcher);
		BasesCommand.register(dispatcher);
		ResourceCommand.register(dispatcher);
		CalculateCommand.register(dispatcher);
		PlacesCommand.register(dispatcher);
		HomeCommand.register(dispatcher);
		SetHomeCommand.register(dispatcher);
		SpawnCommand.register(dispatcher);
		VeinminerCommand.register(dispatcher);
		UndoCommand.register(dispatcher);
		KardExoCommand.register(dispatcher);
		PackCommand.register(dispatcher);
		UptimeCommand.register(dispatcher);
	}
	
	public static void registerTickables(MinecraftServer server)
	{
		server.addTickable(new BasesTickable(server));
	}
	
	public static void setLevelSaving(MinecraftServer server, boolean save)
	{
		for(ServerLevel level : server.getAllLevels())
		{
			if(level != null)
			{
				level.noSave = !save;
			}
		}
	}
	
	public static void stop()
	{
		saveConfigs();
	}
	
	private static void initConfigs(MinecraftServer server)
	{
		CONFIG.setData(new Config());
		VEINMINER.setData(defaultVeinminerConfig(server));
	}
	
	public static void saveConfigs()
	{
		CONFIG_FILES.forEach(ConfigFile::save);
	}
	
	public static void readConfigs()
	{
		CONFIG_FILES.forEach(ConfigFile::read);
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
	
	private static Map<BlockPredicate, VeinConfig> defaultVeinminerConfig(MinecraftServer server)
	{
		Map<BlockPredicate, VeinConfig> config = new HashMap<BlockPredicate, VeinConfig>();
		Registry<Block> registry = server.registryAccess().registry(Registries.BLOCK).orElseThrow();
		
		addVein(Blocks.OAK_LOG, 12, true, registry, config);
		addVein(Blocks.SPRUCE_LOG, 26, true, registry, config);
		addVein(Blocks.JUNGLE_LOG, 26, true, registry, config);
		addVein(Blocks.BIRCH_LOG, 15, true, registry, config);
		addVein(Blocks.DARK_OAK_LOG, 10, true, registry, config);
		addVein(Blocks.ACACIA_LOG, 10, true, registry, config);
		addVein(Blocks.CRIMSON_STEM, 26, true, registry, config);
		addVein(Blocks.WARPED_STEM, 26, true, registry, config);
		addVein(Blocks.MANGROVE_LOG, 10, true, registry, config);
		addVein(Blocks.CHERRY_LOG, 10, true, registry, config);
		
		addVein(Blocks.OAK_LEAVES, 5, true, registry, config);
		addVein(Blocks.SPRUCE_LEAVES, 5, true, registry, config);
		addVein(Blocks.JUNGLE_LEAVES, 5, true, registry, config);
		addVein(Blocks.BIRCH_LEAVES, 5, true, registry, config);
		addVein(Blocks.DARK_OAK_LEAVES, 5, true, registry, config);
		addVein(Blocks.ACACIA_LEAVES, 5, true, registry, config);
		addVein(Blocks.NETHER_WART_BLOCK, 5, true, registry, config);
		addVein(Blocks.WARPED_WART_BLOCK, 5, true, registry, config);
		addVein(Blocks.MANGROVE_LEAVES, 5, true, registry, config);
		addVein(Blocks.CHERRY_LEAVES, 5, true, registry, config);
		
		addVein(Blocks.ANDESITE, 15, true, registry, config);
		addVein(Blocks.DIORITE, 15, true, registry, config);
		addVein(Blocks.GRANITE, 15, true, registry, config);
		
		addVein(Blocks.GRAVEL, 6, true, registry, config);
		addVein(Blocks.GLOWSTONE, 10, false, registry, config);
		addVein(Blocks.SOUL_SAND, 5, true, registry, config);
		addVein(Blocks.OBSIDIAN, 5, true, registry, config);
		addVein(Blocks.CRYING_OBSIDIAN, 5, true, registry, config);
		addVein(Blocks.SAND, 7, true, registry, config);
		addVein(Blocks.RED_SAND, 5, true, registry, config);
		addVein(Blocks.CLAY, 4, true, registry, config);
		
		addVein(BlockTags.COAL_ORES, 17, true, config);
		addVein(BlockTags.COPPER_ORES, 17, true, config);
		addVein(BlockTags.IRON_ORES, 9, false, config);
		addVein(BlockTags.GOLD_ORES, 9, true, config);
		addVein(Blocks.GILDED_BLACKSTONE, 9, true, registry, config);
		addVein(BlockTags.DIAMOND_ORES, 9, true, config);
		addVein(BlockTags.LAPIS_ORES, 7, true, config);
		addVein(BlockTags.REDSTONE_ORES, 8, true, config);
		addVein(Blocks.NETHER_QUARTZ_ORE, 14, true, registry, config);
		addVein(Blocks.ANCIENT_DEBRIS, 3, true, registry, config);
		
		addVein(Blocks.ICE, 10, true, registry, config);
		addVein(Blocks.PACKED_ICE, 10, true, registry, config);
		addVein(Blocks.BLUE_ICE, 10, true, registry, config);
		addVein(Blocks.BONE_BLOCK, 10, true, registry, config);
		addVein(Blocks.WET_SPONGE, 5, true, registry, config);
		
		return config;
	}
	
	private static void addVein(Block block, int radius, boolean requiresTool, Registry<Block> registry, Map<BlockPredicate, VeinConfig> map)
	{
		map.put(new BlockPredicate(registry.getKey(block), null, null, false), new VeinConfig(radius, requiresTool));
	}
	
	private static void addVein(TagKey<Block> tag, int radius, boolean requiresTool, Map<BlockPredicate, VeinConfig> map)
	{
		map.put(new BlockPredicate(tag.location(), null, null, true), new VeinConfig(radius, requiresTool));
	}
}
