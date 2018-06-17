package fr.lespoulpes.backup.incremental.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.lespoulpes.backup.incremental.registry.DiffRegistryEntry.DiffRegistryEntryComparator;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;

public class DiffRegistryBuilder {
	private static final Logger LOG = LogManager.getLogger(DiffRegistryBuilder.class);

	private IRegistry<String, RegistryEntry> older = Registry.EMPTY;
	private IRegistry<String, RegistryEntry> newer;

	public DiffRegistryBuilder older(IRegistry<String, RegistryEntry> older) {
		this.older = older;
		return this;
	}

	public DiffRegistryBuilder newer(IRegistry<String, RegistryEntry> newer) {
		this.newer = newer;
		return this;
	}

	public DiffRegistry build() {
		if (this.older == null || this.newer == null) {
			throw new IllegalArgumentException("Both registry cannot be null");
		}

		if (!this.older.equals(Registry.EMPTY) && !this.older.getSourceDir().equals(this.newer.getSourceDir())) {
			throw new IllegalArgumentException("Registry must refer to the same source dir");
		}

		DiffRegistry registryDiff = new DiffRegistry(newer.getSourceDir(), System.currentTimeMillis(),
				this.older == Registry.EMPTY ? this.newer.getHashAlgorithm() : this.older.getHashAlgorithm());
		Set<String> both = older.getKeys();
		both.retainAll(newer.getKeys());
		LOG.debug("Existing files in both registry : {}", () -> Arrays.toString(both.toArray(new String[0])));

		for (String supposedIdentical : both) {
			RegistryEntry old = older.get(supposedIdentical);
			RegistryEntry newe = newer.get(supposedIdentical);
			if (old.getHash().equals(newe.getHash())) {
				LOG.debug("{} on {} - old hash [{}] vs new hash [{}]", Diff.EQUAL, supposedIdentical, old.getHash(),
						newe.getHash());
				registryDiff.add(Diff.EQUAL, newe);
			} else {
				LOG.debug("{} on {} - old hash [{}] vs new hash [{}]", Diff.UPDATED, supposedIdentical, old.getHash(),
						newe.getHash());
				registryDiff.add(Diff.UPDATED, newe);
			}
		}

		Set<String> deleted = older.getKeys();
		deleted.removeAll(newer.getKeys());
		LOG.debug("Deleted file from old registry : {}", () -> Arrays.toString(deleted.toArray(new String[0])));

		for (String del : deleted) {
			registryDiff.add(Diff.DELETED, older.get(del));
		}

		Set<String> added = newer.getKeys();
		added.removeAll(older.getKeys());
		LOG.debug("Added files in new registry : {}", () -> Arrays.toString(added.toArray(new String[0])));

		for (String add : added) {
			registryDiff.add(Diff.ADDED, newer.get(add));
		}

		return registryDiff;
	}

	public static class RegistryDiffByLineBuilder {
		private DiffRegistry registryDiff;
		private String hashAlgorithm;
		private long timestamp;
		private String sourceDir;

		private RegistryDiffByLineBuilder() {

		}

		public RegistryDiffByLineBuilder sourceDir(String sourceDir) {
			this.sourceDir = sourceDir;
			return this;
		}

		public RegistryDiffByLineBuilder hashAlgorithm(String hashAlgorithm) {
			this.hashAlgorithm = hashAlgorithm;
			return this;
		}

		public RegistryDiffByLineBuilder timestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public RegistryDiffByLineBuilder add(Diff diff, String hash, String path, long size) {
			this.registryDiff = registryDiff == null
					? new DiffRegistry(this.sourceDir, this.timestamp, this.hashAlgorithm)
					: this.registryDiff;
			this.registryDiff.add(new DiffRegistryEntry(diff, hash, path, size));
			return this;
		}

		public DiffRegistry build() {
			return this.registryDiff;
		}
	}

	public static final class DiffRegistry implements IRegistry<Diff, List<DiffRegistryEntry>> {
		public static DiffRegistryBuilder fromTwoRegistry() {
			return new DiffRegistryBuilder();
		}

		public static RegistryDiffByLineBuilder fromLines() {
			return new RegistryDiffByLineBuilder();
		}

		private final String sourceDir;
		private final long timestamp;
		private final String hashAlgorithm;
		private final Map<Diff, List<DiffRegistryEntry>> entries = new LinkedHashMap<>();

		private DiffRegistry(String sourceDir, long timestamp, String hashAlgorithm) {
			this.sourceDir = sourceDir;
			this.hashAlgorithm = hashAlgorithm;
			this.timestamp = timestamp;
		}

		private void add(Diff diff, RegistryEntry entry) {
			this.add(new DiffRegistryEntry(diff, entry));
		}

		private void add(DiffRegistryEntry entry) {
			List<DiffRegistryEntry> typedNodes = this.entries.get(entry.getDiff());
			if (typedNodes == null) {
				typedNodes = new ArrayList<>();
				this.entries.put(entry.getDiff(), typedNodes);
			}
			typedNodes.add(entry);
		}

		public List<DiffRegistryEntry> get(Diff diff) {
			List<DiffRegistryEntry> res = Optional.ofNullable(this.entries.get(diff)).orElse(new ArrayList<>());
			Collections.sort(res, new DiffRegistryEntryComparator());
			return res;
		}

		public String getSourceDir() {
			return sourceDir;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public String getHashAlgorithm() {
			return hashAlgorithm;
		}

		@Override
		public Set<Diff> getKeys() {
			return new TreeSet<>(this.entries.keySet());
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(hashAlgorithm).append(entries).build();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DiffRegistry other = (DiffRegistry) obj;
			EqualsBuilder builder = new EqualsBuilder().append(this.hashAlgorithm, other.hashAlgorithm);
			builder.append(this.entries, other.entries);
			return builder.build();
		}
	}

	public enum Diff {
		ADDED, DELETED, EQUAL, UPDATED;
	}
}
