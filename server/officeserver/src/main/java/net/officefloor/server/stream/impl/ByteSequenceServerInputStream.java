/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
