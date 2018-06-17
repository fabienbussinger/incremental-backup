package fr.lespoulpes.backup.incremental.file.serder;

public interface Serializer<T> {
	void serialize(T registry);
}
