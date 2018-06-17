package fr.lespoulpes.backup.incremental.file.seeking;

import java.util.concurrent.atomic.AtomicLong;

import fr.lespoulpes.backup.incremental.tree.Node;

public interface FileSeeker {
	public Node read();
	
	AtomicLong getFileCounter();
}
