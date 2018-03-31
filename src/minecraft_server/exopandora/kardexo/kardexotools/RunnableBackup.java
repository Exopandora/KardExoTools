package exopandora.kardexo.kardexotools;

import java.io.File;
import java.time.LocalDateTime;

import net.minecraft.util.text.TextComponentString;

public class RunnableBackup implements Runnable
{
	@Override
	public synchronized void run()
	{
		try
		{
			KardExo.notifyPlayers(KardExo.getServer(), new TextComponentString("Starting Backup..."));
			KardExo.saveWorld(false);
			
			LocalDateTime date = LocalDateTime.now();
			String folderName = KardExo.getServer().getFolderName();
			String time = String.format("%02d_%02d_%04d-%02d_%02d_%02d", date.getDayOfMonth(), date.getMonthValue(), date.getYear(), date.getHour(), date.getMinute(), date.getSecond());
			
			if(Config.BACKUP_DIRECTORY.listFiles().length >= Config.BACKUP_FILES)
			{
				File purgeFile = null;
				long lastMod = Long.MAX_VALUE;
				
				for(File file : Config.BACKUP_DIRECTORY.listFiles())
				{
					if(file.getName().contains(KardExo.getServer().getFolderName()))
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
			
			ZipThread zipper = new ZipThread(folderName, new File(Config.BACKUP_DIRECTORY, folderName + "-" + time + ".zip").getPath(), success ->
			{
				if(success)
				{
					KardExo.notifyPlayers(KardExo.getServer(), new TextComponentString("Backup Complete"));
				}
				else
				{
					KardExo.notifyPlayers(KardExo.getServer(), new TextComponentString("Backup Failed"));
				}
			});
			
			zipper.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
