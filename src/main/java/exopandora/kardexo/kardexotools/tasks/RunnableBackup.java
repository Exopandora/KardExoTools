package exopandora.kardexo.kardexotools.tasks;

import java.io.File;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import exopandora.kardexo.kardexotools.KardExo;
import exopandora.kardexo.kardexotools.config.Config;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;;

public class RunnableBackup implements Runnable
{
	private static boolean RUNNING;
	
	@Override
	public synchronized void run()
	{
		if(RunnableBackup.RUNNING)
		{
			KardExo.notifyPlayers(KardExo.getServer(), new StringTextComponent("Backup already in progress"));
		}
		else
		{
			RunnableBackup.RUNNING = true;
			KardExo.notifyPlayers(KardExo.getServer(), new StringTextComponent("Starting backup..."));
			KardExo.saveWorlds(false);
			
			LocalDateTime date = LocalDateTime.now();
			String folderName = KardExo.getServer().getFolderName();
			String time = String.format("%02d_%02d_%04d-%02d_%02d_%02d", date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getHour(), date.getMinute(), date.getSecond());
			String fileName = folderName + "-" + time;
			
			if(Config.BACKUP_DIRECTORY.listFiles().length >= Config.BACKUP_FILES)
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
			
			long start = System.currentTimeMillis();
			
			ZipThread zipper = new ZipThread("backup", folderName, new File(Config.BACKUP_DIRECTORY, fileName + ".zip").getPath(), bytes ->
			{
				if(bytes > 0)
				{
					long duration = System.currentTimeMillis() - start;
					ITextComponent size = new StringTextComponent(FileUtils.byteCountToDisplaySize(bytes));
					size.setStyle(new Style().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TranslationTextComponent("%s\n%s bytes\n%s", fileName, String.format("%,d", bytes), DurationFormatUtils.formatDuration(duration, "HH:mm:ss")))));
					KardExo.notifyPlayers(KardExo.getServer(), new TranslationTextComponent("Backup Complete (%s)", size));
				}
				else
				{
					KardExo.notifyPlayers(KardExo.getServer(), new StringTextComponent("Backup Failed"));
				}
				
				RunnableBackup.RUNNING = false;
			});
			
			zipper.start();
		}
	}
}
