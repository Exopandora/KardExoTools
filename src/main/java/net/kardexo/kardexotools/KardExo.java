package net.kardexo.kardexotools;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
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
import net.kardexo.kardexotools.command.SitCommand;
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
import net.kardexo.kardexotools.tasks.SaveTask;
import net.kardexo.kardexotools.tasks.ShutdownTask;
import net.kardexo.kardexotools.tasks.TaskDispatcher;
import net.kardexo.kardexotools.tasks.TaskScheduler;
import net.kardexo.kardexotools.util.BlockPredicateWrapper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KardExo
{
	public static final String VERSION = FabricLoader.getInstance().getModContainer("kardexotools")
		.map(ModContainer::getMetadata)
		.map(metadata -> metadata.getCustomValue("minecraft").getAsString() + "-" + metadata.getVersion().getFriendlyString())
		.orElseThrow();
	public static final Logger LOGGER = LogManager.getLogger("KardExo");
	
	private static final File CONFIG_DIRECTORY = new File("config/kardexotools");
	
	public static final ConfigFile<Config> CONFIG = new ConfigFile<Config>(new File(CONFIG_DIRECTORY, "config.json"), new TypeToken<Config>() {});
	public static final MapFile<String, Property> BASES = new MapFile<String, Property>(new File(CONFIG_DIRECTORY, "bases.json"), new TypeToken<Map<String, Property>>() {});
	public static final MapFile<String, Property> PLACES = new MapFile<String, Property>(new File(CONFIG_DIRECTORY, "places.json"), new TypeToken<Map<String, Property>>() {});
	public static final MapFile<UUID, PlayerConfig> PLAYERS = new MapFile<UUID, PlayerConfig>(new File(CONFIG_DIRECTORY, "playerdata.json"), new TypeToken<Map<UUID, PlayerConfig>>() {});
	public static final MapFile<BlockPredicateWrapper, VeinConfig> VEINMINER = new MapFile<BlockPredicateWrapper, VeinConfig>(new File(CONFIG_DIRECTORY, "veinminer.json"), new TypeToken<Map<BlockPredicateWrapper, VeinConfig>>() {});
	
	private static final List<ConfigFile<?>> CONFIG_FILES = Lists.newArrayList(CONFIG, BASES, PLACES, PLAYERS, VEINMINER);
	
	public static final SaveTask TASK_SAVE = new SaveTask();
	public static final BackupTask TASK_BACKUP = new BackupTask();
	public static final ShutdownTask TASK_SHUTDOWN = new ShutdownTask();
	
	public static void preInit(MinecraftServer server)
	{
		LOGGER.info("KardExoTools {}", KardExo.VERSION);
		createConfigDirectory();
		initConfigs();
		readConfigs();
		setLevelSaving(server, !CONFIG.getData().isDisableAutoSaving());
		registerTickables(server);
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
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext)
	{
		MoonPhaseCommand.register(dispatcher);
		WhereIsCommand.register(dispatcher);
		BackupCommand.register(dispatcher);
		WorldTimeCommand.register(dispatcher);
		BasesCommand.register(dispatcher, commandBuildContext);
		ResourceCommand.register(dispatcher);
		CalculateCommand.register(dispatcher);
		PlacesCommand.register(dispatcher, commandBuildContext);
		HomeCommand.register(dispatcher);
		SetHomeCommand.register(dispatcher);
		SpawnCommand.register(dispatcher);
		VeinminerCommand.register(dispatcher);
		UndoCommand.register(dispatcher);
		KardExoCommand.register(dispatcher, commandBuildContext);
		PackCommand.register(dispatcher);
		UptimeCommand.register(dispatcher);
		SitCommand.register(dispatcher);
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
	
	private static void initConfigs()
	{
		CONFIG.setData(new Config());
		VEINMINER.setData(defaultVeinminerConfig());
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
			LOGGER.error(e);
		}
	}
	
	private static Map<BlockPredicateWrapper, VeinConfig> defaultVeinminerConfig()
	{
		Map<BlockPredicateWrapper, VeinConfig> config = new HashMap<BlockPredicateWrapper, VeinConfig>();
		
		addVein(Blocks.OAK_LOG, 12, true, config);
		addVein(Blocks.SPRUCE_LOG, 26, true, config);
		addVein(Blocks.JUNGLE_LOG, 26, true, config);
		addVein(Blocks.BIRCH_LOG, 15, true, config);
		addVein(Blocks.DARK_OAK_LOG, 10, true, config);
		addVein(Blocks.ACACIA_LOG, 10, true, config);
		addVein(Blocks.CRIMSON_STEM, 26, true, config);
		addVein(Blocks.WARPED_STEM, 26, true, config);
		addVein(Blocks.MANGROVE_LOG, 10, true, config);
		addVein(Blocks.CHERRY_LOG, 10, true, config);
		addVein(Blocks.PALE_OAK_LOG, 10, true, config);
		
		addVein(Blocks.OAK_LEAVES, 5, true, config);
		addVein(Blocks.SPRUCE_LEAVES, 5, true, config);
		addVein(Blocks.JUNGLE_LEAVES, 5, true, config);
		addVein(Blocks.BIRCH_LEAVES, 5, true, config);
		addVein(Blocks.DARK_OAK_LEAVES, 5, true, config);
		addVein(Blocks.ACACIA_LEAVES, 5, true, config);
		addVein(Blocks.NETHER_WART_BLOCK, 5, true, config);
		addVein(Blocks.WARPED_WART_BLOCK, 5, true, config);
		addVein(Blocks.MANGROVE_LEAVES, 5, true, config);
		addVein(Blocks.CHERRY_LEAVES, 5, true, config);
		addVein(Blocks.AZALEA_LEAVES, 5, true, config);
		addVein(Blocks.PALE_OAK_LEAVES, 5, true, config);
		addVein(Blocks.FLOWERING_AZALEA_LEAVES, 5, true, config);
		
		addVein(Blocks.ANDESITE, 15, true, config);
		addVein(Blocks.DIORITE, 15, true, config);
		addVein(Blocks.GRANITE, 15, true, config);
		
		addVein(Blocks.GRAVEL, 6, true, config);
		addVein(Blocks.GLOWSTONE, 10, false, config);
		addVein(Blocks.SOUL_SAND, 5, true, config);
		addVein(Blocks.OBSIDIAN, 5, true, config);
		addVein(Blocks.CRYING_OBSIDIAN, 5, true, config);
		addVein(Blocks.SAND, 7, true, config);
		addVein(Blocks.RED_SAND, 5, true, config);
		addVein(Blocks.CLAY, 4, true, config);
		
		addVein(BlockTags.COAL_ORES, 17, true, config);
		addVein(BlockTags.COPPER_ORES, 17, true, config);
		addVein(BlockTags.IRON_ORES, 9, false, config);
		addVein(BlockTags.GOLD_ORES, 9, true, config);
		addVein(Blocks.GILDED_BLACKSTONE, 9, true, config);
		addVein(BlockTags.DIAMOND_ORES, 9, true, config);
		addVein(BlockTags.LAPIS_ORES, 7, true, config);
		addVein(BlockTags.REDSTONE_ORES, 8, true, config);
		addVein(Blocks.NETHER_QUARTZ_ORE, 14, true, config);
		addVein(Blocks.ANCIENT_DEBRIS, 3, true, config);
		
		addVein(Blocks.ICE, 10, true, config);
		addVein(Blocks.PACKED_ICE, 10, true, config);
		addVein(Blocks.BLUE_ICE, 10, true, config);
		addVein(Blocks.BONE_BLOCK, 10, true, config);
		addVein(Blocks.WET_SPONGE, 5, true, config);
		
		return config;
	}
	
	private static void addVein(Block block, int radius, boolean requiresTool, Map<BlockPredicateWrapper, VeinConfig> map)
	{
		map.put(new BlockPredicateWrapper(new BlockPredicateArgument.BlockPredicate(block.defaultBlockState(), Collections.emptySet(), null)), new VeinConfig(radius, requiresTool));
	}
	
	private static void addVein(TagKey<Block> tag, int radius, boolean requiresTool, Map<BlockPredicateWrapper, VeinConfig> map)
	{
		map.put(new BlockPredicateWrapper(new BlockPredicateArgument.TagPredicate(BuiltInRegistries.BLOCK.get(tag).orElseThrow(), Collections.emptyMap(), null)), new VeinConfig(radius, requiresTool));
	}
}
