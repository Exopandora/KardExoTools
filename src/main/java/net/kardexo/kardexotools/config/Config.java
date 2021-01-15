package net.kardexo.kardexotools.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.veinminer.VeinminerConfigEntry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class Config
{
	public static final String VERSION = "1.16.5-2.41.8";
	
	//** CONFIGURABLE VALUES **//
	
	/** Time in minutes between saves **/
	public static final int INTERVAL_SAVE = 20;
	
	/** Time in minutes between backups **/
	public static final int INTERVAL_BACKUP = 20;
	
	/** Time in minutes before first save **/
	public static final int OFFSET_SAVE = 10;
	
	/** Time in minutes before first backup **/
	public static final int OFFSET_BACKUP = 20;
	
	/** Time in seconds before the save starts **/
	public static final int[] WARNING_TIMES_SAVE = {5, 10};
	
	/** Warning message displayed before saving **/
	public static final String WARNING_MESSAGE_SAVE = "Saving in %d seconds";
	
	/** Time in seconds before the backup starts **/
	public static final int[] WARNING_TIMES_BACKUP = {5, 10};
	
	/** Warning message displayed before a backup **/
	public static final String WARNING_MESSAGE_BACKUP = "Starting backup in %d seconds";
	
	/** Number of backup files **/
	public static final int BACKUP_FILES = 10;
	
	/** Disable automatic world saving **/
	public static final boolean DISABLE_AUTO_SAVING = false;
	
	/** Number of blocks vein miner can harvest at once **/
	public static final int BLOCK_LIMIT = 128;
	
	/** Number of history entries for the undo command **/
	public static final int HISTORY_SIZE = 5;
	
	/** Backup Directory **/
	public static final File BACKUP_DIRECTORY = new File("backup");
	
	/** Configuration Directory **/
	public static final File CONFIG_DIRECTORY = new File("config");
	
	public static final DataFile<Property, String> BASES = new DataFile<Property, String>(new File(CONFIG_DIRECTORY, "bases.json"), Property[].class, Property::getName);
	public static final DataFile<Property, String> PLACES = new DataFile<Property, String>(new File(CONFIG_DIRECTORY, "places.json"), Property[].class, Property::getName);
	public static final DataFile<PlayerConfig, String> PLAYERS = new DataFile<PlayerConfig, String>(new File(CONFIG_DIRECTORY, "playerdata.json"), PlayerConfig[].class, PlayerConfig::getPlayer);
	public static final DataFile<VeinminerConfigEntry, Block> VEINMINER = new DataFile<VeinminerConfigEntry, Block>(new File(CONFIG_DIRECTORY, "veinminer.json"), VeinminerConfigEntry[].class, VeinminerConfigEntry::toBlock, initial -> 
	{
		initial.add(new VeinminerConfigEntry(Blocks.OAK_LOG, 12, true));
		initial.add(new VeinminerConfigEntry(Blocks.SPRUCE_LOG, 26, true));
		initial.add(new VeinminerConfigEntry(Blocks.JUNGLE_LOG, 26, true));
		initial.add(new VeinminerConfigEntry(Blocks.BIRCH_LOG, 15, true));
		initial.add(new VeinminerConfigEntry(Blocks.DARK_OAK_LOG, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.ACACIA_LOG, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.CRIMSON_STEM, 26, true));
		initial.add(new VeinminerConfigEntry(Blocks.WARPED_STEM, 26, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.OAK_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.SPRUCE_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.JUNGLE_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.BIRCH_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.DARK_OAK_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.ACACIA_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.NETHER_WART_BLOCK, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.WARPED_WART_BLOCK, 5, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.ANDESITE, 15, true));
		initial.add(new VeinminerConfigEntry(Blocks.DIORITE, 15, true));
		initial.add(new VeinminerConfigEntry(Blocks.GRANITE, 15, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.GRAVEL, 6, true));
		initial.add(new VeinminerConfigEntry(Blocks.GLOWSTONE, 10, false));
		initial.add(new VeinminerConfigEntry(Blocks.SOUL_SAND, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.OBSIDIAN, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.CRYING_OBSIDIAN, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.SAND, 7, true));
		initial.add(new VeinminerConfigEntry(Blocks.RED_SAND, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.CLAY, 4, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.COAL_ORE, 17, true));
		initial.add(new VeinminerConfigEntry(Blocks.IRON_ORE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.GOLD_ORE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.GILDED_BLACKSTONE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.DIAMOND_ORE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.LAPIS_ORE, 7, true));
		initial.add(new VeinminerConfigEntry(Blocks.REDSTONE_ORE, 8, true));
		initial.add(new VeinminerConfigEntry(Blocks.NETHER_QUARTZ_ORE, 14, true));
		initial.add(new VeinminerConfigEntry(Blocks.NETHER_GOLD_ORE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.ANCIENT_DEBRIS, 3, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.ICE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.PACKED_ICE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.BLUE_ICE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.BONE_BLOCK, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.WET_SPONGE, 5, true));
	});
	
	public static void commands(List<Consumer<CommandDispatcher<CommandSource>>> commands)
	{
		//List of all available commands
		commands.add(CommandMoonPhase::register);
		commands.add(CommandWhereIs::register);
		commands.add(CommandBackup::register);
		commands.add(CommandWorldTime::register);
		commands.add(CommandBases::register);
		commands.add(CommandResource::register);
		commands.add(CommandCalculate::register);
		commands.add(CommandPlaces::register);
		commands.add(CommandHome::register);
		commands.add(CommandSetHome::register);
		commands.add(CommandSpawn::register);
		commands.add(CommandVeinminer::register);
		commands.add(CommandUndo::register);
		commands.add(CommandKardExo::register);
		commands.add(CommandSetBiome::register);
	}
	
	//** DO NOT EDIT BELOW THIS LINE **//
	
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(BlockPos.class, new BlockPosTypeAdapter())
			.disableHtmlEscaping()
			.setPrettyPrinting()
			.create();
	
	public static void saveAllFiles()
	{
		Config.save(BASES);
		Config.save(PLACES);
		Config.save(PLAYERS);
		Config.save(VEINMINER);
	}
	
	public static void readAllFiles() throws Exception
	{
		Config.read(BASES);
		Config.read(PLACES);
		Config.read(PLAYERS);
		Config.read(VEINMINER);
	}
	
	public static void save(DataFile<?, ?> file)
	{
		Config.createConfigDirectory();
		file.save(GSON);
	}
	
	public static void read(DataFile<?, ?> file) throws Exception
	{
		Config.createConfigDirectory();
		file.read(GSON);
	}
	
	private static void createConfigDirectory()
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
}
