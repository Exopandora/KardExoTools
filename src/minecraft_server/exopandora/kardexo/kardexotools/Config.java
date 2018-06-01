package exopandora.kardexo.kardexotools;

import java.io.File;

public class Config
{	
	public static final String VERSION = "1.12.2-2.27";
	
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
	public static final int BACKUP_FILES = 5;
	
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
	public static final DataFile<Home, String> HOME = new DataFile<Home, String>(new File(CONFIG_DIRECTORY, "home.json"), Home[].class, Home::getPlayer);
	public static final DataFile<VeinminerOption, String> VEINMINER = new DataFile<VeinminerOption, String>(new File(CONFIG_DIRECTORY, "veinminer.json"), VeinminerOption[].class, VeinminerOption::getPlayer);
	
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
		VEINMINER.save();
	}
	
	public static void readAllFiles()
	{
		BASES.read();
		PLACES.read();
		HOME.read();
		VEINMINER.read();
	}
}
