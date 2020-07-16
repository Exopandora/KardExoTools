package net.kardexo.kardexotools.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

public class TaskBackup extends AbstractTask
{
	private static boolean BACKUP_IN_PROGRESS;
	
	public TaskBackup(MinecraftServer server)
	{
		super(server);
	}
	
	@Override
	public void run()
	{
		if(BACKUP_IN_PROGRESS)
		{
			KardExo.notifyPlayers(this.getServer(), new StringTextComponent("Backup already in progress"));
		}
		else
		{
			BACKUP_IN_PROGRESS = true;
			long start = System.currentTimeMillis();
			
			KardExo.notifyPlayers(this.getServer(), new StringTextComponent("Starting backup..."));
			KardExo.saveWorlds(this.getServer(), false);
			
			LocalDateTime date = LocalDateTime.now();
			
			String folderName = this.getServer().anvilConverterForAnvilFile.func_237282_a_();
			String time = String.format("%02d_%02d_%04d-%02d_%02d_%02d", date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getHour(), date.getMinute(), date.getSecond());
			String fileName = folderName + "-" + time;
			
			this.createDirectories();
			this.purgeFiles(folderName);
			
			ZipThread zipper = new ZipThread("backup", Paths.get(folderName), Config.BACKUP_DIRECTORY.toPath().resolve(fileName + ".zip"), file ->
			{
				this.printResult(file, start);
				BACKUP_IN_PROGRESS = false;
			});
			zipper.start();
		}
	}
	
	private void createDirectories()
	{
		try
		{
			Files.createDirectories(Config.BACKUP_DIRECTORY.toPath());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void purgeFiles(String folderName)
	{
		if(Config.BACKUP_DIRECTORY.exists() && Config.BACKUP_DIRECTORY.canWrite() && Config.BACKUP_DIRECTORY.listFiles().length >= Config.BACKUP_FILES)
		{
			File purgeFile = null;
			long lastMod = Long.MAX_VALUE;
			
			for(File file : Config.BACKUP_DIRECTORY.listFiles())
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
			StringTextComponent size = new StringTextComponent(FileUtils.byteCountToDisplaySize(bytes));
			ITextComponent hover = new TranslationTextComponent("%s\n%s bytes\n%s", file.getName(), String.format("%,d", bytes), DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));
			size.func_230530_a_(Style.field_240709_b_.func_240716_a_(new HoverEvent(Action.field_230550_a_, hover)));
			KardExo.notifyPlayers(this.getServer(), new TranslationTextComponent("Backup Complete (%s)", size));
		}
		else
		{
			KardExo.notifyPlayers(this.getServer(), new StringTextComponent("Backup Failed"));
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
		return String.format(Config.WARNING_MESSAGE_BACKUP, seconds);
	}
	
	@Override
	public boolean requiresPlayers()
	{
		return true;
	}
}
