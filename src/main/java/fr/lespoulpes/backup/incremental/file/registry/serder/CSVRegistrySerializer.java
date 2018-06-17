package fr.lespoulpes.backup.incremental.file.registry.serder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;
import fr.lespoulpes.backup.incremental.file.serder.Serializer;
import fr.lespoulpes.backup.incremental.registry.RegistryEntry;

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
			for (String path : registry.getKeys()) {
				RegistryEntry entry = registry.get(path);
				bw.write(line(entry));
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

	private String line(RegistryEntry re) {
		return String.format("%s;%s;%s", re.getHash(), re.getPath(), re.getSize());
	}
}
