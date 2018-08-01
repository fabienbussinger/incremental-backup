package fr.lespoulpes.backup.incremental.tree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private final Path node;
	private final String hash;
	private final long size;
	private final List<Node> children = new ArrayList<Node>();

    public Node(Path node, String hash, final long size) {
		this.node = node;
		this.hash = hash;
		this.size = size;
	}

    public Path getNode() {
		return node;
	}
	public String getHash() {
		return hash;
	}
	
	public long getSize() {
		return size;
	}

	public List<Node> getChildren() {
		return children;
	}
}
