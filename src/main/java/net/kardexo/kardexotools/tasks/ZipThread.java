package net.kardexo.kardexotools.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class ZipThread extends Thread
{
	private final Path srcFolder;
	private final Path destZipFile;
	private final Consumer<File> callback;
	
	public ZipThread(String name, Path srcFolder, Path destZipFile, Consumer<File> callback)
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
			this.callback.accept(ZipThread.zip(this.srcFolder, this.destZipFile));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			this.callback.accept(null);
		}
	}
	
	private static File zip(Path sourceDir, Path outputFile) throws IOException, FileNotFoundException
	{
		Files.createDirectories(outputFile.getParent());
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outputFile.toFile()));
	    ZipThread.zip(sourceDir, zip);
		IOUtils.closeQuietly(zip);
		
		return outputFile.toFile();
	}
	
	private static void zip(Path sourceDir, ZipOutputStream zip) throws IOException, FileNotFoundException
	{
		for(File file : sourceDir.toFile().listFiles())
		{
			if(file.isDirectory())
			{
				ZipThread.zip(file.toPath(), zip);
			}
			else if(!file.getName().equals("session.lock"))
			{
	            ZipEntry entry = new ZipEntry(file.getPath());
	            zip.putNextEntry(entry);
	            
	            FileInputStream input = new FileInputStream(file);
	            IOUtils.copy(input, zip);
				IOUtils.closeQuietly(input);
			}
		}
	}
}
