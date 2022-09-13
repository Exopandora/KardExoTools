package net.kardexo.kardexotools.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;

import net.kardexo.kardexotools.KardExo;

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
	
	@Override
	public void run()
	{
		try
		{
			this.callback.accept(ZipThread.zip(this.srcFolder, this.destZipFile));
		}
		catch(Exception e)
		{
			this.callback.accept(null);
			e.printStackTrace();
		}
	}
	
	private static File zip(Path sourceDir, Path outputFile) throws IOException, FileNotFoundException, InterruptedException, ExecutionException
	{
		Files.createDirectories(outputFile.getParent());
		FileOutputStream outputStream = new FileOutputStream(outputFile.toFile());
		
		try(ZipArchiveOutputStream zip = new ZipArchiveOutputStream(outputStream))
		{
			ExecutorService executor = Executors.newFixedThreadPool(KardExo.CONFIG.getData().getBackupThreadCount());
			ParallelScatterZipCreator zipCreator = new ParallelScatterZipCreator(executor);
			ZipThread.zip(sourceDir.toAbsolutePath().getParent(), sourceDir, zip, zipCreator);
			zipCreator.writeTo(zip);
		}
		
		return outputFile.toFile();
	}
	
	private static void zip(Path root, Path sourceDir, ZipArchiveOutputStream zip, ParallelScatterZipCreator zipCreator) throws IOException, FileNotFoundException
	{
		for(File file : sourceDir.toFile().listFiles())
		{
			if(file.isDirectory())
			{
				ZipThread.zip(root, file.toPath(), zip, zipCreator);
			}
			else if(!file.getName().equals("session.lock"))
			{
				String relativePath = file.getAbsolutePath().substring(root.toAbsolutePath().toString().length() + 1);
				InputStreamSupplier streamSupplier = () ->
				{
					try
					{
						return Files.newInputStream(file.toPath());
					}
					catch(IOException e)
					{
						return InputStream.nullInputStream();
					}
				};
				ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(relativePath);
				zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
				zipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
			}
		}
	}
}
