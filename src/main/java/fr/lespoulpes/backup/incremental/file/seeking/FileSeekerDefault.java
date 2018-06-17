package fr.lespoulpes.backup.incremental.file.seeking;

import java.io.File;

import fr.lespoulpes.backup.incremental.tree.Node;
import fr.lespoulpes.backup.incremental.tree.NodeBuilder;

public class FileSeekerDefault extends AbstractFileSeeker {
	private final File sourceDirectory;
	private final NodeBuilder nodeBuilder;

	public FileSeekerDefault(String sourceDirectory, String hasher) {
		this.nodeBuilder = new NodeBuilder(hasher);
		File mayBeDir = new File(sourceDirectory);
		if (!mayBeDir.exists() || !mayBeDir.isDirectory()) {
			throw new RuntimeException("Cannot operate on regular file as source");
		}
		this.sourceDirectory = mayBeDir;
	}

	private Node explore(File current) {
		Node currentNode = this.nodeBuilder.create(current);
		this.getFileCounter().incrementAndGet();
		File[] files = current.listFiles();
		if (files == null) {
			return currentNode;
		} else {
			for (File file : files) {
				currentNode.getChildren().add(this.explore(file));
			}
			return currentNode;
		}
	}

	@Override
	protected Node startReading() {
		return explore(this.sourceDirectory);
	}
}
