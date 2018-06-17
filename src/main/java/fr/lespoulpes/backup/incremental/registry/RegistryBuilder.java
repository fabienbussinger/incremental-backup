package fr.lespoulpes.backup.incremental.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fr.lespoulpes.backup.incremental.tree.Node;

public class RegistryBuilder {
	private String sourceDir;
	private long timestamp;
	private String hashAlgorithm;
	private Node node;
	private final List<RegistryEntry> entries = new ArrayList<>();

	public RegistryBuilder timestamp(long timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public RegistryBuilder hashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
		return this;
	}

	public RegistryBuilder node(Node node) {
		this.node = node;
		return this;
	}

	public RegistryBuilder sourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
		return this;
	}
	
	public RegistryBuilder entry(RegistryEntry entry) {
		this.entries.add(entry);
		return this;
	}

	public Registry build() {
		if ((node == null && entries.isEmpty())
				|| !Arrays.stream(MessageDigestAlgorithms.values()).anyMatch((s) -> s.equals(hashAlgorithm))) {
			throw new IllegalArgumentException("Both node and hashAlgorithm should be provided");
		}
		Registry registry = new Registry(sourceDir, timestamp, hashAlgorithm);
		if (node != null) {
			this.build(registry, node);
		} else {
			this.entries.forEach(registry::add);
		}

		return registry;
	}

	private void build(Registry registry, Node node) {
		registry.add(node);
		for (Node child : node.getChildren()) {
			build(registry, child);
		}
	}

	public static class Registry implements IRegistry<String, RegistryEntry> {
		public static final Registry EMPTY = new Registry("", 0, "NoAlgorithm");

		public static RegistryBuilder builder() {
			return new RegistryBuilder();
		}

		private final String sourceDir;
		private final long timestamp;
		private final String hashAlgorithm;
		private final Map<String, RegistryEntry> entries = new TreeMap<>();

		public Registry(String sourceDir, long timestamp, String hashAlgorithm) {
			this.sourceDir = sourceDir;
			this.timestamp = timestamp;
			this.hashAlgorithm = hashAlgorithm;
		}

		public String getSourceDir() {
			return sourceDir;
		}


		public long getTimestamp() {
			return timestamp;
		}

		private void add(Node node) {
			String absolutePath = StringUtils.remove(node.getNode().getAbsolutePath(), this.sourceDir);
			this.add(new RegistryEntry(node.getHash(), FilenameUtils.separatorsToUnix(absolutePath), node.getSize()));
		}

		protected void add(RegistryEntry entry) {
			this.entries.put(entry.getPath(), entry);
		}

		public Set<String> getKeys() {
			return new TreeSet<>(this.entries.keySet());
		}

		public RegistryEntry get(String key) {
			return this.entries.get(key);
		}

		public String getHashAlgorithm() {
			return hashAlgorithm;
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
			Registry other = (Registry) obj;
			return new EqualsBuilder().append(this.hashAlgorithm, other.hashAlgorithm)
					.append(this.entries, other.entries).build();
		}

	}


}
