package exopandora.kardexo.kardexotools;

import java.io.File;

public class Config
{	
	public static final String VERSION = "1.12.2-2.19";
	
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
	
	/** Backup Directory **/
	public static final File BACKUP_DIRECTORY = new File("backup");
	
	public static final DataFile<Property, String> BASES = new DataFile<Property, String>("bases.json", Property[].class, Property::getName);
	public static final DataFile<Property, String> PLACES = new DataFile<Property, String>("places.json", Property[].class, Property::getName);
	
	static
	{
		if(!BACKUP_DIRECTORY.exists())
		{
			BACKUP_DIRECTORY.mkdirs();
		}
	}
}
