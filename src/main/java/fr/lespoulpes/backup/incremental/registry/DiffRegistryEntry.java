package fr.lespoulpes.backup.incremental.registry;

import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.nio.file.Path;
import java.util.Comparator;

public class DiffRegistryEntry extends RegistryEntry {
	private final Diff diff;

    public DiffRegistryEntry(Diff diff, String hash, Path path, long size) {
		super(hash, path, size);
		this.diff = diff;
	}
	
	public DiffRegistryEntry(Diff diff, RegistryEntry entry) {
		super(entry);
		this.diff = diff;
	}

	public Diff getDiff() {
		return diff;
	}

	public static class DiffRegistryEntryComparator implements Comparator<DiffRegistryEntry> {

		@Override
		public int compare(DiffRegistryEntry o1, DiffRegistryEntry o2) {
			return new CompareToBuilder().append(o1.diff, o2.diff).append(o1.getHash(), o2.getHash())
					.append(o1.getPath(), o2.getPath()).toComparison();
		}

	}

}
