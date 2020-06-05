package net.kardexo.kardexotools.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipThread extends Thread
{
	private final String srcFolder;
	private final String destZipFile;
	private final Consumer<Long> callback;
	
	public ZipThread(String name, String srcFolder, String destZipFile, Consumer<Long> callback)
	{
		super(name);
		this.srcFolder = srcFolder;
		this.destZipFile = destZipFile;
		this.callback = callback;
	}
	
	public void run()
	{
		try
		{
			this.callback.accept(this.zipFolder(this.srcFolder, this.destZipFile));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			this.callback.accept(0L);
		}
	}
	
	private long zipFolder(String srcFolder, String destZipFile) throws Exception
	{
		FileOutputStream fileOut = new FileOutputStream(destZipFile);
		ZipOutputStream zipOut = new ZipOutputStream(fileOut);
		
		this.addFolderToZip("", srcFolder, zipOut);

		fileOut.flush();
		zipOut.flush();
		
		long size = fileOut.getChannel().size();
		
		zipOut.close();
		fileOut.close();
		
		return size;
	}
	
	private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception
	{
		File folder = new File(srcFolder);
		
		for(String fileName : folder.list())
		{
			if(path.equals(""))
			{
				this.addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			}
			else
			{
				this.addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
			}
		}
	}
	
	private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception
	{
		File folder = new File(srcFile);
		
		if(folder.isDirectory())
		{
			this.addFolderToZip(path, srcFile, zip);
		}
		else
		{
			FileInputStream input = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			
			int length;
			byte[] buffer = new byte[1024];
			
			while((length = input.read(buffer)) > 0)
			{
				zip.write(buffer, 0, length);
			}
			
			input.close();
		}
	}
}
