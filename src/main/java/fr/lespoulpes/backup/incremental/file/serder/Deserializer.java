package fr.lespoulpes.backup.incremental.file.serder;

public interface Deserializer<T> {
	T deserialize();
}
