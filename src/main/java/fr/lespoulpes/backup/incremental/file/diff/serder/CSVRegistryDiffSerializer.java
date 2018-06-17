package fr.lespoulpes.backup.incremental.file.diff.serder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import fr.lespoulpes.backup.incremental.file.RegistryFilenameUtils;
import fr.lespoulpes.backup.incremental.file.serder.Serializer;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryEntry;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;

public class CSVRegistryDiffSerializer implements Serializer<DiffRegistry> {
	private final File destination;

	public CSVRegistryDiffSerializer(File destination) {
		this.destination = destination;
	}

	@Override
	public void serialize(DiffRegistry registry) {
		String registryFilename = RegistryFilenameUtils.toFilename(registry);
		File registryFile = new File(this.destination, registryFilename);
		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(registryFile))) {
			bw.write(String.format("%s;%s;%s", registry.getSourceDir(), registry.getTimestamp(), registry.getHashAlgorithm()));
			bw.newLine();
			for (Diff diff : Diff.values()) {
				for (DiffRegistryEntry node : registry.get(diff)) {
					bw.write(line(node));
					bw.newLine();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String line(DiffRegistryEntry re) {
		return String.format("%s;%s;%s;%s", re.getDiff(), re.getHash(), re.getPath(), re.getSize());
	}
}
