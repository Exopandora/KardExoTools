package net.kardexo.kardexotools.config;

import java.io.File;
import java.time.Duration;

import com.google.gson.annotations.SerializedName;

public class Config
{
	@SerializedName("backup_enabled")
	private boolean backupEnabled = true;
	@SerializedName("backup_files")
	private int backupFiles = 24;
	@SerializedName("backup_directory")
	private String backupDirectory = "backup";
	@SerializedName("backup_interval")
	private long backupInterval = Duration.ofHours(3).toSeconds();
	@SerializedName("backup_offset")
	private long backupOffset = Duration.ofHours(3).toSeconds();
	@SerializedName("backup_warning_times")
	private long[] backupWarningTimes = {5, 10};
	@SerializedName("backup_warning_message")
	private String backupWarningMessage = "Starting backup in %d seconds";
	@SerializedName("backup_thread_count")
	private int backupThreadCount = 4;
	
	@SerializedName("shutdown_enabled")
	private boolean shutdownEnabled = false;
	@SerializedName("shutdown_backup")
	private boolean shutdownBackup = true;
	@SerializedName("shutdown_offset")
	private long shutdownOffset = Duration.ofHours(6).toSeconds();
	@SerializedName("shutdown_warning_times")
	private long[] shutdownWarningTimes = {1, 5, 10};
	@SerializedName("shutdown_warning_message")
	private String shutdownWarningMessage = "Shutting down in %d minutes";
	@SerializedName("shutdown_message")
	private String shutdownMessage = "multiplayer.disconnect.server_shutdown";
	
	@SerializedName("save_enabled")
	private boolean saveEnabled = true;
	@SerializedName("save_flush")
	private boolean saveFlush = true;
	@SerializedName("save_interval")
	private long saveInterval = Duration.ofMinutes(30).toSeconds();
	@SerializedName("save_offset")
	private long saveOffset = Duration.ofMinutes(30).toSeconds();
	@SerializedName("save_warning_times")
	private long[] saveWarningTimes = {5, 10};
	@SerializedName("save_warning_message")
	private String saveWarningMessage = "Saving in %d seconds";
	
	@SerializedName("disable_auto_saving")
	private boolean disableAutoSaving = false;
	
	@SerializedName("veinminer_block_limit")
	private int veinminerBlockLimit = 128;
	@SerializedName("veinminer_history_size")
	private int veinminerHistorySize = 5;
	
	@SerializedName("property_default_enter_message")
	private String propertyDefaultEnterMessage = "%1$s has entered your base (%2$s)";
	@SerializedName("property_default_exit_message")
	private String propertyDefaultExitMessage = "%1$s has left your base (%2$s)";
	
	@SerializedName("backup_command_enabled")
	private boolean backupCommandEnabled = true;
	@SerializedName("bases_command_enabled")
	private boolean basesCommandEnabled = true;
	@SerializedName("calculate_command_enabled")
	private boolean calculateCommandEnabled = true;
	@SerializedName("home_command_enabled")
	private boolean homeCommandEnabled = true;
	@SerializedName("moonphase_command_enabled")
	private boolean moonphaseCommandEnabled = true;
	@SerializedName("pack_command_enabled")
	private boolean packCommandEnabled = true;
	@SerializedName("places_command_enabled")
	private boolean placesCommandEnabled = true;
	@SerializedName("resource_command_enabled")
	private boolean resourceCommandEnabled = true;
	@SerializedName("sethome_command_enabled")
	private boolean sethomeCommandEnabled = true;
	@SerializedName("spawn_command_enabled")
	private boolean spawnCommandEnabled = true;
	@SerializedName("undo_command_enabled")
	private boolean undoCommandEnabled = true;
	@SerializedName("veinminer_command_enabled")
	private boolean veinminerCommandEnabled = true;
	@SerializedName("whereis_command_enabled")
	private boolean whereisCommandEnabled = true;
	@SerializedName("worldtime_command_enabled")
	private boolean worldtimeCommandEnabled = true;
	
	@SerializedName("harvest_crops_with_right_click")
	private boolean harvestCropsWithRightClick = true;
	
	@SerializedName("pickup_leash_knots_with_right_click")
	private boolean pickupLeashKnots = true;
	
	public boolean isBackupEnabled()
	{
		return this.backupEnabled;
	}
	
	public void setBackupEnabled(boolean backupEnabled)
	{
		this.backupEnabled = backupEnabled;
	}
	
	public int getBackupFiles()
	{
		return this.backupFiles;
	}
	
	public void setBackupFiles(int backupFiles)
	{
		this.backupFiles = backupFiles;
	}
	
	public File getBackupDirectory()
	{
		return new File(this.backupDirectory);
	}
	
	public void setBackupDirectory(String backupDirectory)
	{
		this.backupDirectory = backupDirectory;
	}
	
	public long getBackupInterval()
	{
		return this.backupInterval;
	}
	
	public void setBackupInterval(long backupInterval)
	{
		this.backupInterval = backupInterval;
	}
	
	public long getBackupOffset()
	{
		return this.backupOffset;
	}
	
	public void setBackupOffset(long backupOffset)
	{
		this.backupOffset = backupOffset;
	}
	
	public long[] getBackupWarningTimes()
	{
		return this.backupWarningTimes;
	}
	
	public void setBackupWarningTimes(long[] backupWarningTimes)
	{
		this.backupWarningTimes = backupWarningTimes;
	}
	
	public String getBackupWarningMessage()
	{
		return this.backupWarningMessage;
	}
	
	public void setBackupWarningMessage(String backupWarningMessage)
	{
		this.backupWarningMessage = backupWarningMessage;
	}
	
	public boolean isShutdownEnabled()
	{
		return this.shutdownEnabled;
	}
	
	public void setShutdownEnabled(boolean shutdownEnabled)
	{
		this.shutdownEnabled = shutdownEnabled;
	}
	
	public boolean isShutdownBackup()
	{
		return this.shutdownBackup;
	}
	
	public void setShutdownBackup(boolean shutdownBackup)
	{
		this.shutdownBackup = shutdownBackup;
	}
	
	public long getShutdownOffset()
	{
		return this.shutdownOffset;
	}
	
	public void setShutdownOffset(long shutdownOffset)
	{
		this.shutdownOffset = shutdownOffset;
	}
	
	public long[] getShutdownWarningTimes()
	{
		return this.shutdownWarningTimes;
	}
	
	public void setShutdownWarningTimes(long[] shutdownWarningTimes)
	{
		this.shutdownWarningTimes = shutdownWarningTimes;
	}
	
	public String getShutdownWarningMessage()
	{
		return this.shutdownWarningMessage;
	}
	
	public void setShutdownWarningMessage(String shutdownWarningMessage)
	{
		this.shutdownWarningMessage = shutdownWarningMessage;
	}
	
	public String getShutdownMessage()
	{
		return this.shutdownMessage;
	}
	
	public void setShutdownMessage(String shutdownMessage)
	{
		this.shutdownMessage = shutdownMessage;
	}
	
	public boolean isSaveEnabled()
	{
		return this.saveEnabled;
	}
	
	public void setSaveEnabled(boolean saveEnabled)
	{
		this.saveEnabled = saveEnabled;
	}
	
	public boolean isSaveFlush()
	{
		return this.saveFlush;
	}
	
	public void setSaveFlush(boolean saveFlush)
	{
		this.saveFlush = saveFlush;
	}
	
	public long getSaveInterval()
	{
		return this.saveInterval;
	}
	
	public void setSaveInterval(long saveInterval)
	{
		this.saveInterval = saveInterval;
	}
	
	public long getSaveOffset()
	{
		return this.saveOffset;
	}
	
	public void setSaveOffset(long saveOffset)
	{
		this.saveOffset = saveOffset;
	}
	
	public long[] getSaveWarningTimes()
	{
		return this.saveWarningTimes;
	}
	
	public void setSaveWarningTimes(long[] saveWarningTimes)
	{
		this.saveWarningTimes = saveWarningTimes;
	}
	
	public String getSaveWarningMessage()
	{
		return this.saveWarningMessage;
	}
	
	public void setSaveWarningMessage(String saveWarningMessage)
	{
		this.saveWarningMessage = saveWarningMessage;
	}
	
	public boolean isDisableAutoSaving()
	{
		return this.disableAutoSaving;
	}
	
	public void setDisableAutoSaving(boolean disableAutoSaving)
	{
		this.disableAutoSaving = disableAutoSaving;
	}
	
	public int getVeinminerBlockLimit()
	{
		return this.veinminerBlockLimit;
	}
	
	public void setVeinminerBlockLimit(int veinminerBlockLimit)
	{
		this.veinminerBlockLimit = veinminerBlockLimit;
	}
	
	public int getVeinminerHistorySize()
	{
		return this.veinminerHistorySize;
	}
	
	public void setVeinminerHistorySize(int veinminerHistorySize)
	{
		this.veinminerHistorySize = veinminerHistorySize;
	}
	
	public String getPropertyDefaultEnterMessage()
	{
		return this.propertyDefaultEnterMessage;
	}
	
	public void setPropertyDefaultEnterMessage(String propertyDefaultEnterMessage)
	{
		this.propertyDefaultEnterMessage = propertyDefaultEnterMessage;
	}
	
	public String getPropertyDefaultExitMessage()
	{
		return this.propertyDefaultExitMessage;
	}
	
	public void setPropertyDefaultExitMessage(String propertyDefaultExitMessage)
	{
		this.propertyDefaultExitMessage = propertyDefaultExitMessage;
	}
	
	public int getBackupThreadCount()
	{
		return this.backupThreadCount > 0 ? Math.min(this.backupThreadCount, Runtime.getRuntime().availableProcessors()) : Runtime.getRuntime().availableProcessors();
	}
	
	public void setBackupThreadCount(int backupThreadCount)
	{
		this.backupThreadCount = backupThreadCount;
	}
	
	public boolean isBackupCommandEnabled()
	{
		return this.backupCommandEnabled;
	}
	
	public void setBackupCommandEnabled(boolean backupCommandEnabled)
	{
		this.backupCommandEnabled = backupCommandEnabled;
	}
	
	public boolean isBasesCommandEnabled()
	{
		return this.basesCommandEnabled;
	}
	
	public void setBasesCommandEnabled(boolean basesCommandEnabled)
	{
		this.basesCommandEnabled = basesCommandEnabled;
	}
	
	public boolean isCalculateCommandEnabled()
	{
		return this.calculateCommandEnabled;
	}
	
	public void setCalculateCommandEnabled(boolean calculateCommandEnabled)
	{
		this.calculateCommandEnabled = calculateCommandEnabled;
	}
	
	public boolean isHomeCommandEnabled()
	{
		return this.homeCommandEnabled;
	}
	
	public void setHomeCommandEnabled(boolean homeCommandEnabled)
	{
		this.homeCommandEnabled = homeCommandEnabled;
	}
	
	public boolean isMoonphaseCommandEnabled()
	{
		return this.moonphaseCommandEnabled;
	}
	
	public void setMoonphaseCommandEnabled(boolean moonphaseCommandEnabled)
	{
		this.moonphaseCommandEnabled = moonphaseCommandEnabled;
	}
	
	public boolean isPackCommandEnabled()
	{
		return this.packCommandEnabled;
	}
	
	public void setPackCommandEnabled(boolean packCommandEnabled)
	{
		this.packCommandEnabled = packCommandEnabled;
	}
	
	public boolean isPlacesCommandEnabled()
	{
		return this.placesCommandEnabled;
	}
	
	public void setPlacesCommandEnabled(boolean placesCommandEnabled)
	{
		this.placesCommandEnabled = placesCommandEnabled;
	}
	
	public boolean isResourceCommandEnabled()
	{
		return this.resourceCommandEnabled;
	}
	
	public void setResourceCommandEnabled(boolean resourceCommandEnabled)
	{
		this.resourceCommandEnabled = resourceCommandEnabled;
	}
	
	public boolean isSethomeCommandEnabled()
	{
		return this.sethomeCommandEnabled;
	}
	
	public void setSethomeCommandEnabled(boolean sethomeCommandEnabled)
	{
		this.sethomeCommandEnabled = sethomeCommandEnabled;
	}
	
	public boolean isSpawnCommandEnabled()
	{
		return this.spawnCommandEnabled;
	}
	
	public void setSpawnCommandEnabled(boolean spawnCommandEnabled)
	{
		this.spawnCommandEnabled = spawnCommandEnabled;
	}
	
	public boolean isUndoCommandEnabled()
	{
		return this.undoCommandEnabled;
	}
	
	public void setUndoCommandEnabled(boolean undoCommandEnabled)
	{
		this.undoCommandEnabled = undoCommandEnabled;
	}
	
	public boolean isVeinminerCommandEnabled()
	{
		return this.veinminerCommandEnabled;
	}
	
	public void setVeinminerCommandEnabled(boolean veinminerCommandEnabled)
	{
		this.veinminerCommandEnabled = veinminerCommandEnabled;
	}
	
	public boolean isWhereisCommandEnabled()
	{
		return this.whereisCommandEnabled;
	}
	
	public void setWhereisCommandEnabled(boolean whereisCommandEnabled)
	{
		this.whereisCommandEnabled = whereisCommandEnabled;
	}
	
	public boolean isWorldtimeCommandEnabled()
	{
		return this.worldtimeCommandEnabled;
	}
	
	public void setWorldtimeCommandEnabled(boolean worldtimeCommandEnabled)
	{
		this.worldtimeCommandEnabled = worldtimeCommandEnabled;
	}
	
	public boolean doHarvestCropsWithRightClick()
	{
		return this.harvestCropsWithRightClick;
	}
	
	public void setHarvestCropsWithRightClick(boolean harvestCropsWithRightClick)
	{
		this.harvestCropsWithRightClick = harvestCropsWithRightClick;
	}
	
	public boolean doPickupLeashKnots()
	{
		return this.pickupLeashKnots;
	}
	
	public void setPickupLeashKnots(boolean pickupLeashKnots)
	{
		this.pickupLeashKnots = pickupLeashKnots;
	}
}