package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.server.stream.ServerInputStream;

/**
 * {@link ByteSequence} {@link ServerInputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteSequenceServerInputStream extends ServerInputStream {

	/**
	 * {@link ByteSequence}.
	 */
	private final ByteSequence byteSequence;

	/**
	 * Position within the {@link ByteSequence}.
	 */
	private int position;

	/**
	 * Instantiate.
	 * 
	 * @param byteSequence
	 *            {@link ByteSequence}.
	 * @param position
	 *            Starting position within the {@link ByteSequence}.
	 */
	public ByteSequenceServerInputStream(ByteSequence byteSequence, int position) {
		this.byteSequence = byteSequence;
		this.position = position;
	}

	/*
	 * ================== ServerInputStream ======================
	 */

	@Override
	public InputStream createBrowseInputStream() {
		return new ByteSequenceServerInputStream(this.byteSequence, this.position);
	}

	@Override
	public int read() throws IOException {
		if (this.position >= this.byteSequence.length()) {
			return -1; // end of stream
		} else {
			// Return next byte
			return this.byteSequence.byteAt(this.position++);
		}
	}

	@Override
	public int available() throws IOException {
		return Math.max(0, this.byteSequence.length() - position);
	}

}
