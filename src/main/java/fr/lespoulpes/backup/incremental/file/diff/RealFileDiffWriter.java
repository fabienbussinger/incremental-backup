package fr.lespoulpes.backup.incremental.file.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import fr.lespoulpes.backup.incremental.Constants;
import fr.lespoulpes.backup.incremental.file.RegistryFilenameUtils;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryEntry;

public class RealFileDiffWriter {
	private final File destination;

	public RealFileDiffWriter(File destination) {
		this.destination = destination;
	}

	public void write(DiffRegistry registry) {
		String contentFileName = this.destination.getAbsolutePath() + File.separator
				+ RegistryFilenameUtils.toFilenamePrefix(registry) + ".zip";
		File sourceDir = new File(registry.getSourceDir());
		try (ZipOutputStream zof = new ZipOutputStream(new FileOutputStream(contentFileName))) {
			zof.setLevel(0);
			for (Diff aDiffType : Diff.values()) {
				for (DiffRegistryEntry entry : registry.get(aDiffType)) {
					if (!entry.getHash().equals(Constants.DIRECTORY_HASH) && !(entry.getDiff() == Diff.DELETED || entry.getDiff() == Diff.EQUAL)) {
						ZipEntry zipEntry = new ZipEntry(sourceDir.getName() + entry.getPath());
						zof.putNextEntry(zipEntry);
						try (FileInputStream source = new FileInputStream(new File(sourceDir, entry.getPath()))) {
							IOUtils.copy(source, zof);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
