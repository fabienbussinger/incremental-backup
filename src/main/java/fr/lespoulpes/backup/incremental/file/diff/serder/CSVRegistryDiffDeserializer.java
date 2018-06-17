package fr.lespoulpes.backup.incremental.file.diff.serder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import fr.lespoulpes.backup.incremental.file.serder.Deserializer;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.RegistryDiffByLineBuilder;

public class CSVRegistryDiffDeserializer implements Deserializer<DiffRegistry> {
	private final File file;

	public CSVRegistryDiffDeserializer(File file) {
		this.file = file;
	}

	@Override
	public DiffRegistry deserialize() {
		try (final BufferedReader br = new BufferedReader(new FileReader(this.file))) {
			String[] headers = br.readLine().split(";");
			RegistryDiffByLineBuilder builder = DiffRegistry.fromLines().sourceDir(headers[0]).timestamp(Long.parseLong(headers[1]))
					.hashAlgorithm(headers[1]);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(";");
				builder.add(Diff.valueOf(values[0]), values[1], values[2], Long.parseLong(values[3]));
			}
			return builder.build();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
