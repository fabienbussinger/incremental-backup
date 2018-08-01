package fr.lespoulpes.backup.incremental.file.registry.serder;

import fr.lespoulpes.backup.incremental.file.serder.Serializer;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;
import fr.lespoulpes.backup.incremental.registry.RegistryEntry;

import java.io.*;
import java.nio.file.Path;

public class CSVRegistrySerializer implements Serializer<Registry> {
	private final File file;

	public CSVRegistrySerializer(File file) {
		this.file = file;
	}

	@Override
	public void serialize(Registry registry) {
		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(this.file))) {
			bw.write(String.format("%s;%s;%s", registry.getSourceDir(), registry.getTimestamp(), registry.getHashAlgorithm()));
			bw.newLine();
			for (Path path : registry.getKeys()) {
				RegistryEntry entry = registry.get(path);
				bw.write(line(registry, entry));
				bw.newLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String line(Registry registry, RegistryEntry re) {
		return String.format("%s;%s;%s", re.getHash(), registry.getSourceDir().relativize(re.getPath()).normalize(), re.getSize());
	}
}
