package fr.lespoulpes.backup.incremental.file.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnzipIncrementalArchive {
	private static final Logger LOG = LogManager.getLogger(UnzipIncrementalArchive.class);
	private final File destination;
	public UnzipIncrementalArchive(File destination) {
		this.destination = destination;
	}

	public void unzip(File zip) {
		LOG.info("Unziping {} to {}", zip.getAbsolutePath(), this.destination.getAbsolutePath());
		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
			ZipEntry ze;
			while((ze = zis.getNextEntry()) != null) {
				File current = new File(destination,ze.getName());
				
				if(ze.isDirectory()) {
					current.mkdirs();
				} else {
					//ensure that path exists
					File parent = current;
					while(!(parent = parent.getParentFile()).equals(destination)) {
						parent.mkdirs();
					}
					try(FileOutputStream fos = new FileOutputStream(current)) {
						IOUtils.copyLarge(zis, fos);
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Zip source file cannot be found", e);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read input file", e);
		}
	}
}
