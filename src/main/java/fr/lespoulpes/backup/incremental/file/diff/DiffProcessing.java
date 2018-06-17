package fr.lespoulpes.backup.incremental.file.diff;

import java.io.File;

import fr.lespoulpes.backup.incremental.file.diff.serder.CSVRegistryDiffSerializer;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;

public class DiffProcessing {
	private final File destination;

	public DiffProcessing(File destination) {
		this.destination = destination;
	}

	public void process(DiffRegistry registry) {
		new CSVRegistryDiffSerializer(this.destination).serialize(registry);
		new RealFileDiffWriter(this.destination).write(registry);
		
	}
}
