package fr.lespoulpes.backup.incremental.file.registry.serder;

import fr.lespoulpes.backup.incremental.file.serder.Deserializer;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;
import fr.lespoulpes.backup.incremental.registry.RegistryEntry;

import java.io.*;
import java.nio.file.Paths;

public class CSVRegistryDeserializer implements Deserializer<Registry> {
	private final File file;

	public CSVRegistryDeserializer(File file) {
		this.file = file;
	}

	@Override
	public Registry deserialize() {
		try (final BufferedReader br = new BufferedReader(new FileReader(this.file))) {
			String[] headers = br.readLine().split(";");
			RegistryBuilder builder = Registry.builder().sourceDir(Paths.get(headers[0])).timestamp(Long.parseLong(headers[1]))
					.hashAlgorithm(headers[2]);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(";");
				builder.entry(new RegistryEntry(values[0], Paths.get(headers[0], values[1]), Long.parseLong(values[2])));
			}
			return builder.build();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
