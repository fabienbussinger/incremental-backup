package fr.lespoulpes.backup.incremental.file.diff;

import fr.lespoulpes.backup.incremental.Constants;
import fr.lespoulpes.backup.incremental.file.RegistryFilenameUtils;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryEntry;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RealFileDiffWriter {
    private final int compressionLevel;
	private final File destination;

    public RealFileDiffWriter(int compressionLevel, File destination) {
        this.compressionLevel = compressionLevel;
        this.destination = destination;
    }

    public RealFileDiffWriter(File destination) {
        this(0, destination);
    }

	public void write(DiffRegistry registry) {
		String contentFileName = this.destination.getAbsolutePath() + File.separator
				+ RegistryFilenameUtils.toFilenamePrefix(registry) + ".zip";
		File sourceDir = new File(registry.getSourceDir());
		try (ZipOutputStream zof = new ZipOutputStream(new FileOutputStream(contentFileName))) {
            zof.setLevel(compressionLevel);
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
