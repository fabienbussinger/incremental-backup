package fr.lespoulpes.backup.incremental.registry;

import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.Diff;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;

public class DiffRegistryToRegistryAdapter extends Registry {

	public DiffRegistryToRegistryAdapter(DiffRegistry source) {
		super(source.getSourceDir(), source.getTimestamp(), source.getHashAlgorithm());
		for (Diff diff : Diff.values()) {
			if(diff != Diff.DELETED) {
				for (DiffRegistryEntry entry : source.get(diff)) {
					this.add(entry);
				}				
			}
		}
	}
}
