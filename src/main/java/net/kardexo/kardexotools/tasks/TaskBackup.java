package net.kardexo.kardexotools.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;

public class TaskBackup extends AbstractTask
{
	private static boolean backupInProgress;
	
	public TaskBackup(MinecraftServer server)
	{
		super(server);
	}
	
	@Override
	public void run()
	{
		
		
		if(TaskBackup.backupInProgress)
		{
			KardExo.notifyPlayers(this.server, new TextComponent("Backup already in progress"));
		}
		else
		{
			TaskBackup.backupInProgress = true;
			long start = System.currentTimeMillis();
			
			KardExo.notifyPlayers(this.server, new TextComponent("Starting backup..."));
			KardExo.saveLevels(this.server, false);
			
			LocalDateTime date = LocalDateTime.now();
			
			String folderName = this.server.storageSource.getLevelId();
			String time = String.format("%02d_%02d_%04d-%02d_%02d_%02d", date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getHour(), date.getMinute(), date.getSecond());
			String fileName = folderName + "-" + time;
			
			this.createDirectories();
			this.purgeFiles(folderName);
			
			ZipThread zipper = new ZipThread("backup", Paths.get(folderName), KardExo.CONFIG.getBackupDirectory().toPath().resolve(fileName + ".zip"), file ->
			{
				this.printResult(file, start);
				TaskBackup.backupInProgress = false;
			});
			zipper.start();
		}
	}
	
	private void createDirectories()
	{
		try
		{
			Files.createDirectories(KardExo.CONFIG.getBackupDirectory().toPath());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void purgeFiles(String folderName)
	{
		File backupDirectory = KardExo.CONFIG.getBackupDirectory();
		
		if(backupDirectory.exists() && backupDirectory.canWrite() && backupDirectory.listFiles().length >= KardExo.CONFIG.getBackupFiles())
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
	
	private void printResult(File file, long start)
	{
		if(file != null)
		{
			long bytes = file.length();
			long duration = System.currentTimeMillis() - start;
			TextComponent size = new TextComponent(FileUtils.byteCountToDisplaySize(bytes));
			Component hover = new TranslatableComponent("%s\n%s bytes\n%s", file.getName(), String.format("%,d", bytes), DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));
			size.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover)));
			KardExo.notifyPlayers(this.server, new TranslatableComponent("Backup Complete (%s)", size));
		}
		else
		{
			KardExo.notifyPlayers(this.server, new TextComponent("Backup Failed"));
		}
	}
	
	@Override
	public String getName()
	{
		return "backup";
	}
	
	@Override
	public String getWarningMessage(long seconds)
	{
		return String.format(KardExo.CONFIG.getBackupWarningMessage(), seconds);
	}
	
	@Override
	public boolean requiresPlayers()
	{
		return true;
	}
}
