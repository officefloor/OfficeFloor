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
