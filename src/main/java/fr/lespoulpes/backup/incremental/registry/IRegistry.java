package fr.lespoulpes.backup.incremental.registry;

import java.nio.file.Path;
import java.util.Set;

public interface IRegistry<K, V> {
    Path getSourceDir();
	
	long getTimestamp();

	String getHashAlgorithm();

	Set<K> getKeys();

	V get(K key);
}
