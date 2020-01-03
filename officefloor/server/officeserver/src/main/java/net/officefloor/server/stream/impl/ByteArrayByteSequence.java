package net.officefloor.server.stream.impl;

import java.io.Serializable;

/**
 * <code>byte</code> array {@link ByteSequence}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteArrayByteSequence implements ByteSequence, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bytes backing this {@link ByteSequence}.
	 */
	private final byte[] bytes;

	/**
	 * Instantiate.
	 * 
	 * @param bytes
	 *            Bytes backing this {@link ByteSequence}.
	 */
	public ByteArrayByteSequence(byte[] bytes) {
		this.bytes = bytes;
	}

	/*
	 * ===================== ByteSequence ========================
	 */

	@Override
	public byte byteAt(int index) {
		return this.bytes[index];
	}

	@Override
	public int length() {
		return this.bytes.length;
	}

}