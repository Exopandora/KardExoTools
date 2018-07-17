package exopandora.kardexo.kardexotools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class Config
{	
	public static final String VERSION = "1.12.2-2.33";
	
	/** Time in minutes between saves **/
	public static final int INTERVAL_SAVE = 20;
	
	/** Time in minutes between backups **/
	public static final int INTERVAL_BACKUP = 20;
	
	/** Time in minutes before first save **/
	public static final int OFFSET_SAVE = 10;
	
	/** Time in minutes before first backup **/
	public static final int OFFSET_BACKUP = 20;
	
	/** Time in seconds before the save starts **/
	public static final int WARNING_DELAY_SAVE = 5;
	
	/** Time in seconds before the backup starts **/
	public static final int WARNING_DELAY_BACKUP = 5;
	
	/** Number of backup files **/
	public static final int BACKUP_FILES = 10;
	
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
	public static final DataFile<Home, String> HOME = new DataFile<Home, String>(new File(CONFIG_DIRECTORY, "home.json"), Home[].class, Home::getPlayer);
	public static final DataFile<PlayerData, String> PLAYERS = new DataFile<PlayerData, String>(new File(CONFIG_DIRECTORY, "playerdata.json"), PlayerData[].class, PlayerData::getPlayer);
	public static final DataFile<VeinminerEntry, IBlockState> VEINMINER = new DataFile<VeinminerEntry, IBlockState>(new File(CONFIG_DIRECTORY, "veinminer.json"), VeinminerEntry[].class, VeinminerEntry::toBlockState,() -> 
	{
		Set<VeinminerEntry> initial = new HashSet<VeinminerEntry>();
		
		initial.add(VeinminerEntry.fromBlockState(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK), 12));
		initial.add(VeinminerEntry.fromBlockState(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE), 26));
		initial.add(VeinminerEntry.fromBlockState(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE), 26));
		initial.add(VeinminerEntry.fromBlockState(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH), 15));
		
		initial.add(VeinminerEntry.fromBlockState(Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK), 10));
		initial.add(VeinminerEntry.fromBlockState(Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA), 10));
		
		initial.add(VeinminerEntry.fromBlockState(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), 15));
		initial.add(VeinminerEntry.fromBlockState(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), 15));
		initial.add(VeinminerEntry.fromBlockState(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), 15));
		
		initial.add(VeinminerEntry.fromBlockState(Blocks.GRAVEL.getDefaultState(), 10));
		initial.add(VeinminerEntry.fromBlockState(Blocks.GLOWSTONE.getDefaultState(), 10));
		initial.add(VeinminerEntry.fromBlockState(Blocks.SOUL_SAND.getDefaultState(), 5));
		initial.add(VeinminerEntry.fromBlockState(Blocks.OBSIDIAN.getDefaultState(), 5));
		initial.add(VeinminerEntry.fromBlockState(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.SAND), 5));
		initial.add(VeinminerEntry.fromBlockState(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND), 5));
		initial.add(VeinminerEntry.fromBlockState(Blocks.CLAY.getDefaultState(), 5));
		
		initial.add(VeinminerEntry.fromBlockState(Blocks.COAL_ORE.getDefaultState(), 17));
		initial.add(VeinminerEntry.fromBlockState(Blocks.IRON_ORE.getDefaultState(), 9));
		initial.add(VeinminerEntry.fromBlockState(Blocks.GOLD_ORE.getDefaultState(), 9));
		initial.add(VeinminerEntry.fromBlockState(Blocks.DIAMOND_ORE.getDefaultState(), 9));
		initial.add(VeinminerEntry.fromBlockState(Blocks.LAPIS_ORE.getDefaultState(), 7));
		initial.add(VeinminerEntry.fromBlockState(Blocks.REDSTONE_ORE.getDefaultState(), 8));
		initial.add(VeinminerEntry.fromBlockState(Blocks.QUARTZ_ORE.getDefaultState(), 14));
		
		initial.add(VeinminerEntry.fromBlockState(Blocks.PACKED_ICE.getDefaultState(), 10));
		initial.add(VeinminerEntry.fromBlockState(Blocks.BONE_BLOCK.getDefaultState(), 10));
		
		return initial;
	});
	
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
		HOME.save();
		PLAYERS.save();
		VEINMINER.save();
	}
	
	public static void readAllFiles()
	{
		BASES.read();
		PLACES.read();
		HOME.read();
		PLAYERS.read();
		VEINMINER.read();
	}
}
