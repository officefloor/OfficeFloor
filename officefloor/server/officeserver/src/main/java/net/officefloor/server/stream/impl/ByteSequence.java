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
