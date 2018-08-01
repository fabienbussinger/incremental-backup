package fr.lespoulpes.backup.incremental.file.diff.serder;

import fr.lespoulpes.backup.incremental.file.serder.Deserializer;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.RegistryDiffByLineBuilder;

import java.io.*;
import java.nio.file.Paths;

public class CSVRegistryDiffDeserializer implements Deserializer<DiffRegistry> {
	private final File file;

	public CSVRegistryDiffDeserializer(File file) {
		this.file = file;
	}

	@Override
	public DiffRegistry deserialize() {
		try (final BufferedReader br = new BufferedReader(new FileReader(this.file))) {
			String[] headers = br.readLine().split(";");
			RegistryDiffByLineBuilder builder = DiffRegistry.fromLines().sourceDir(Paths.get(headers[0])).timestamp(Long.parseLong(headers[1]))
					.hashAlgorithm(headers[1]);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(";");
				builder.add(Diff.valueOf(values[0]), values[1], Paths.get(headers[0], values[2]), Long.parseLong(values[3]));
			}
			return builder.build();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
