/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
