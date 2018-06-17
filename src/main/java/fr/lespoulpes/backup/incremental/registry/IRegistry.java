package fr.lespoulpes.backup.incremental.registry;

import java.util.Set;

public interface IRegistry<K, V> {
	String getSourceDir();
	
	long getTimestamp();

	String getHashAlgorithm();

	Set<K> getKeys();

	V get(K key);
}
