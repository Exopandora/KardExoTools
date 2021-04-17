package net.kardexo.kardexotools.config;

import java.io.File;

import com.google.gson.annotations.SerializedName;

public class Config
{
	@SerializedName("backup_files")
	private int backupFiles = 10;
	@SerializedName("backup_directory")
	private String backupDirectory = "backup";
	@SerializedName("backup_interval")
	private int backupInterval = 20;
	@SerializedName("backup_offset")
	private int backupOffset = 20;
	@SerializedName("backup_warning_times")
	private int[] backupWarningTimes = {5, 10};
	@SerializedName("backup_warning_message")
	private String backupWarningMessage = "Starting backup in %d seconds";
	
	@SerializedName("save_interval")
	private int saveInterval = 20;
	@SerializedName("save_offset")
	private int saveOffset = 10;
	@SerializedName("save_warning_times")
	private int[] saveWarningTimes = {5, 10};
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
	
	public File getBackupDirectory()
	{
		return new File(this.backupDirectory);
	}
	
	public void setBackupDirectory(File backupDirectory)
	{
		this.backupDirectory = backupDirectory.getName();
	}
	
	public int getBackupInterval()
	{
		return this.backupInterval;
	}
	
	public void setBackupInterval(int backupInterval)
	{
		this.backupInterval = backupInterval;
	}
	
	public int getBackupOffset()
	{
		return this.backupOffset;
	}
	
	public void setBackupOffset(int backupOffset)
	{
		this.backupOffset = backupOffset;
	}
	
	public int[] getBackupWarningTimes()
	{
		return this.backupWarningTimes;
	}
	
	public void setBackupWarningTimes(int[] backupWarningTimes)
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
	
	public int getSaveInterval()
	{
		return this.saveInterval;
	}
	
	public void setSaveInterval(int saveInterval)
	{
		this.saveInterval = saveInterval;
	}
	
	public int getSaveOffset()
	{
		return this.saveOffset;
	}
	
	public void setSaveOffset(int saveOffset)
	{
		this.saveOffset = saveOffset;
	}
	
	public int[] getSaveWarningTimes()
	{
		return this.saveWarningTimes;
	}
	
	public void setSaveWarningTimes(int[] saveWarningTimes)
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
	
	public int getBackupFiles()
	{
		return this.backupFiles;
	}
	
	public void setBackupFiles(int backupFiles)
	{
		this.backupFiles = backupFiles;
	}
	
	public boolean doDisableAutoSaving()
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
	
	public void setPropertydefaultEnterMessage(String propertyDefaultEnterMessage)
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
}