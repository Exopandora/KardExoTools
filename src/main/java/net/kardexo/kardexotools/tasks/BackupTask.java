package net.kardexo.kardexotools.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.mixin.MinecraftServerAccessor;
import net.kardexo.kardexotools.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

public class BackupTask implements ITask
{
	private static boolean backupInProgress;
	
	@Override
	public void execute(MinecraftServer server)
	{
		if(BackupTask.backupInProgress)
		{
			Util.broadcastMessage(server, Component.literal("Backup already in progress"));
		}
		else
		{
			BackupTask.backupInProgress = true;
			long start = System.currentTimeMillis();
			
			Util.broadcastMessage(server, Component.literal("Starting backup..."));
			Util.saveLevels(server, false);
			KardExo.setLevelSaving(server, false);
			
			LocalDateTime date = LocalDateTime.now();
			String folderName = ((MinecraftServerAccessor) server).getStorageSource().getLevelId();
			String time = String.format("%02d_%02d_%04d-%02d_%02d_%02d", date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getHour(), date.getMinute(), date.getSecond());
			String fileName = folderName + "-" + time;
			
			this.createDirectories();
			this.purgeFiles(folderName);
			
			ZipThread zipper = new ZipThread("backup", Paths.get(folderName), KardExo.CONFIG.getData().getBackupDirectory().toPath().resolve(fileName + ".zip"), file ->
			{
				this.printResult(server, file, start);
				server.execute(() -> KardExo.setLevelSaving(server, !KardExo.CONFIG.getData().isDisableAutoSaving()));
				BackupTask.backupInProgress = false;
			});
			zipper.start();
		}
	}
	
	private void createDirectories()
	{
		try
		{
			Files.createDirectories(KardExo.CONFIG.getData().getBackupDirectory().toPath());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void purgeFiles(String folderName)
	{
		File backupDirectory = KardExo.CONFIG.getData().getBackupDirectory();
		
		if(backupDirectory.exists() && backupDirectory.canWrite() && backupDirectory.listFiles().length >= KardExo.CONFIG.getData().getBackupFiles())
		{
			File purgeFile = null;
			long lastMod = Long.MAX_VALUE;
			
			for(File file : backupDirectory.listFiles())
			{
				if(file.getName().contains(folderName))
				{
					if(file.lastModified() < lastMod)
					{
						lastMod = file.lastModified();
						purgeFile = file;
					}
				}
			}
			
			if(purgeFile != null)
			{
				purgeFile.delete();
			}
		}
	}
	
	private void printResult(MinecraftServer server, File file, long start)
	{
		if(file != null)
		{
			long bytes = file.length();
			long duration = System.currentTimeMillis() - start;
			MutableComponent size = Component.literal(FileUtils.byteCountToDisplaySize(bytes));
			Component hover = Component.translatable("%s\n%s bytes\n%s", file.getName(), String.format("%,d", bytes), DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));
			size.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover)));
			Util.broadcastMessage(server, Component.translatable("Backup Complete (%s)", size));
		}
		else
		{
			Util.broadcastMessage(server, Component.literal("Backup Failed"));
		}
	}
	
	@Override
	public String getName()
	{
		return "backup";
	}
	
	@Override
	public long getOffset()
	{
		return KardExo.CONFIG.getData().getBackupOffset();
	}
	
	@Override
	public TimeUnit getOffsetTimeUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public long getInterval()
	{
		return KardExo.CONFIG.getData().getBackupInterval();
	}
	
	@Override
	public TimeUnit getIntervalTimeUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public long[] getWarningTimes()
	{
		return KardExo.CONFIG.getData().getBackupWarningTimes();
	}
	
	@Override
	public TimeUnit getWarningTimesUnit()
	{
		return TimeUnit.SECONDS;
	}
	
	@Override
	public String getWarningMessage(long millis)
	{
		return String.format(KardExo.CONFIG.getData().getBackupWarningMessage(), this.getWarningTimesUnit().convert(millis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public int getPriority()
	{
		return 100;
	}
	
	@Override
	public boolean isEnabled()
	{
		return KardExo.CONFIG.getData().isBackupEnabled();
	}
}
