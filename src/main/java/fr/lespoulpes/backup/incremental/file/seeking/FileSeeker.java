package fr.lespoulpes.backup.incremental.file.seeking;

import fr.lespoulpes.backup.incremental.tree.Node;

import java.util.concurrent.atomic.AtomicLong;

public interface FileSeeker {
    Node read();

    AtomicLong getFileCounter();
}
