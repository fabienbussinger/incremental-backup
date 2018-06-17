package fr.lespoulpes.backup.incremental.file.seeking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestOutputStream extends ByteArrayOutputStream {
	private final MessageDigest digest;

	public MessageDigestOutputStream(String shaAlgo) {
		try {
			this.digest = MessageDigest.getInstance(shaAlgo);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void write(int b) {
		ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
		bb.putInt(b);
		this.digest.update(bb.array());
	}

	@Override
	public synchronized void write(byte[] input, int offset, int len) {
		this.digest.update(input, offset, len);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void write(byte[] input) throws IOException {
		this.digest.update(input);
	}

	public byte[] getDigest() {
		return this.digest.digest();
	}
}
