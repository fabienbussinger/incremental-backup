package fr.lespoulpes.backup.incremental.registry;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.nio.file.Path;
import java.util.Comparator;

public class RegistryEntry {
	private final String hash;
    private final Path path;
	private final long size;

    public RegistryEntry(String hash, Path path, long size) {
		this.hash = hash;
		this.path = path;
		this.size = size;
	}

	public RegistryEntry(RegistryEntry entry) {
		this(entry.hash, entry.path, entry.size);
	}

	public String getHash() {
		return hash;
	}

    public Path getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegistryEntry other = (RegistryEntry) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (path == null) {
            return other.path == null;
        } else return path.equals(other.path);
    }

	public static final class RegistryEntryComparator implements Comparator<RegistryEntry> {

		@Override
		public int compare(RegistryEntry o1, RegistryEntry o2) {
			return new CompareToBuilder().append(o1.getHash(), o2.getHash()).append(o1.getPath(), o2.getPath())
					.toComparison();
		}

	}
}
