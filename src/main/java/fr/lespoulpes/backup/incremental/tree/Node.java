package fr.lespoulpes.backup.incremental.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Node {
	private final File node;
	private final String hash;
	private final long size;
	private final List<Node> children = new ArrayList<Node>();
	public Node(File node, String hash, final long size) {
		this.node = node;
		this.hash = hash;
		this.size = size;
	}
	
	public File getNode() {
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
