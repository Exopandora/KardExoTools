package net.kardexo.kardexotools.config;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import net.kardexo.kardexotools.command.CommandBackup;
import net.kardexo.kardexotools.command.CommandBases;
import net.kardexo.kardexotools.command.CommandCalculate;
import net.kardexo.kardexotools.command.CommandForceSave;
import net.kardexo.kardexotools.command.CommandHome;
import net.kardexo.kardexotools.command.CommandKardExo;
import net.kardexo.kardexotools.command.CommandLocateBiome;
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

public class Config
{
	public static final String VERSION = "1.15.2-2.40.3";
	
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
	
	/** Maximum radius to search for a biome **/
	public static final int LOCATE_BIOME_RADIUS = 10000;
	
	/** Factor of the in-game day length. Has to be greater than 0 **/
	public static final float DAYTIME_FACTOR = 1.0F;
	
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
		
		initial.add(new VeinminerConfigEntry(Blocks.OAK_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.SPRUCE_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.JUNGLE_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.BIRCH_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.DARK_OAK_LEAVES, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.ACACIA_LEAVES, 5, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.ANDESITE, 15, true));
		initial.add(new VeinminerConfigEntry(Blocks.DIORITE, 15, true));
		initial.add(new VeinminerConfigEntry(Blocks.GRANITE, 15, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.GRAVEL, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.GLOWSTONE, 10, false));
		initial.add(new VeinminerConfigEntry(Blocks.SOUL_SAND, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.OBSIDIAN, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.SAND, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.RED_SAND, 5, true));
		initial.add(new VeinminerConfigEntry(Blocks.CLAY, 5, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.COAL_ORE, 17, true));
		initial.add(new VeinminerConfigEntry(Blocks.IRON_ORE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.GOLD_ORE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.DIAMOND_ORE, 9, true));
		initial.add(new VeinminerConfigEntry(Blocks.LAPIS_ORE, 7, true));
		initial.add(new VeinminerConfigEntry(Blocks.REDSTONE_ORE, 8, true));
		initial.add(new VeinminerConfigEntry(Blocks.NETHER_QUARTZ_ORE, 14, true));
		
		initial.add(new VeinminerConfigEntry(Blocks.ICE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.PACKED_ICE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.BLUE_ICE, 10, true));
		initial.add(new VeinminerConfigEntry(Blocks.BONE_BLOCK, 10, true));
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
		commands.add(CommandForceSave::register);
		commands.add(CommandCalculate::register);
		commands.add(CommandPlaces::register);
		commands.add(CommandHome::register);
		commands.add(CommandSetHome::register);
		commands.add(CommandSpawn::register);
		commands.add(CommandVeinminer::register);
		commands.add(CommandUndo::register);
		commands.add(CommandLocateBiome::register);
		commands.add(CommandKardExo::register);
		commands.add(CommandSetBiome::register);
	}
	
	//** DO NOT EDIT BELOW THIS LINE **//
	
	static
	{
		if(!BACKUP_DIRECTORY.exists())
		{
			BACKUP_DIRECTORY.mkdirs();
		}
		
		if(!CONFIG_DIRECTORY.exists())
		{
			CONFIG_DIRECTORY.mkdirs();
		}
	}
	
	public static void saveAllFiles()
	{
		BASES.save();
		PLACES.save();
		PLAYERS.save();
		VEINMINER.save();
	}
	
	public static void readAllFiles() throws Exception
	{
		BASES.read();
		PLACES.read();
		PLAYERS.read();
		VEINMINER.read();
	}
}
