package fr.lespoulpes.backup.incremental.registry;

import fr.lespoulpes.backup.incremental.registry.DiffRegistryEntry.DiffRegistryEntryComparator;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

public class DiffRegistryBuilder {
    private static final Logger LOG = LogManager.getLogger(DiffRegistryBuilder.class);

    private IRegistry<Path, RegistryEntry> older = Registry.EMPTY;
    private IRegistry<Path, RegistryEntry> newer;

    public DiffRegistryBuilder older(IRegistry<Path, RegistryEntry> older) {
        this.older = older;
        return this;
    }

    public DiffRegistryBuilder newer(IRegistry<Path, RegistryEntry> newer) {
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
        Set<Path> both = older.getKeys();
        both.retainAll(newer.getKeys());
        LOG.debug("Existing files in both registry : {}", () -> Arrays.toString(both.toArray(new Path[0])));

        for (Path supposedIdentical : both) {
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

        Set<Path> deleted = older.getKeys();
        deleted.removeAll(newer.getKeys());
        LOG.debug("Deleted file from old registry : {}", () -> Arrays.toString(deleted.toArray(new Path[0])));

        for (Path del : deleted) {
            registryDiff.add(Diff.DELETED, older.get(del));
        }

        Set<Path> added = newer.getKeys();
        added.removeAll(older.getKeys());
        LOG.debug("Added files in new registry : {}", () -> Arrays.toString(added.toArray(new Path[0])));

        for (Path add : added) {
            registryDiff.add(Diff.ADDED, newer.get(add));
        }

        return registryDiff;
    }

    public enum Diff {
        ADDED, DELETED, EQUAL, UPDATED
    }

    public static class RegistryDiffByLineBuilder {
        private DiffRegistry registryDiff;
        private String hashAlgorithm;
        private long timestamp;
        private Path sourceDir;

        private RegistryDiffByLineBuilder() {

        }

        public RegistryDiffByLineBuilder sourceDir(Path sourceDir) {
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

        public RegistryDiffByLineBuilder add(Diff diff, String hash, Path path, long size) {
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
        private final Path sourceDir;
        private final long timestamp;
        private final String hashAlgorithm;
        private final Map<Diff, List<DiffRegistryEntry>> entries = new LinkedHashMap<>();

        private DiffRegistry(Path sourceDir, long timestamp, String hashAlgorithm) {
            this.sourceDir = sourceDir;
            this.hashAlgorithm = hashAlgorithm;
            this.timestamp = timestamp;
        }

        public static DiffRegistryBuilder fromTwoRegistry() {
            return new DiffRegistryBuilder();
        }

        public static RegistryDiffByLineBuilder fromLines() {
            return new RegistryDiffByLineBuilder();
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

        public boolean isInitial() {
            Set<Diff> shouldBeEmpty = EnumSet.of(Diff.DELETED, Diff.EQUAL, Diff.UPDATED);
            int total = 0;
            for (Diff diff : shouldBeEmpty) {
                total += Optional.ofNullable(this.entries.get(diff)).orElse(new ArrayList<>()).size();
            }
            return total == 0 && Optional.ofNullable(this.entries.get(Diff.ADDED)).orElse(new ArrayList<>()).size() > 0;
        }

        public List<DiffRegistryEntry> get(Diff diff) {
            List<DiffRegistryEntry> res = Optional.ofNullable(this.entries.get(diff)).orElse(new ArrayList<>());
            Collections.sort(res, new DiffRegistryEntryComparator());
            return res;
        }

        public Path getSourceDir() {
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
}
