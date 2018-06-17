package fr.lespoulpes.backup.incremental.tree;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import fr.lespoulpes.backup.incremental.Constants;
import fr.lespoulpes.backup.incremental.file.seeking.MessageDigestOutputStream;

public class NodeBuilder {
	private final String shaAlgo;

	public NodeBuilder(final String shaAlgo) {
		this.shaAlgo = shaAlgo;
	}

	public Node create(File node) {
		if (node.isDirectory()) {
			return new Node(node, Constants.DIRECTORY_HASH, 0);
		} else {
			try {
				MessageDigestOutputStream mdos = new MessageDigestOutputStream(shaAlgo);
				FileUtils.copyFile(node, mdos);

				return new Node(node, Hex.encodeHexString(mdos.getDigest()), node.length());
			} catch (IOException e) {
				throw new RuntimeException("Unable to read file " + node.getAbsolutePath(), e);
			}
		}
	}
}
