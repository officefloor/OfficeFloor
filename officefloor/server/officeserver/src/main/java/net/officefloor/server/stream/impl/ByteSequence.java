package net.officefloor.server.stream.impl;

import java.util.NoSuchElementException;

/**
 * Sequence of <code>byte</code> values.
 * 
 * @author Daniel Sagenschneider
 */
public interface ByteSequence {

	/**
	 * Empty {@link ByteSequence}.
	 */
	public static final ByteSequence EMPTY = new ByteSequence() {

		@Override
		public byte byteAt(int index) {
			throw new NoSuchElementException();
		}

		@Override
		public int length() {
			return 0;
		}
	};

	/**
	 * Obtains the <code>byte</code> at the index.
	 * 
	 * @param index
	 *            Index of the <code>byte</code>.
	 * @return <code>byte</code> at the index.
	 */
	byte byteAt(int index);

	/**
	 * Obtains the number of <code>byte</code> values in the
	 * {@link ByteSequence}.
	 * 
	 * @return Number of <code>byte</code> values in the {@link ByteSequence}.
	 */
	int length();

}
