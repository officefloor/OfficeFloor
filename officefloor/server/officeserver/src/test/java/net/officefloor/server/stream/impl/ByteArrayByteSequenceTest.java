/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.stream.impl;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link ByteArrayByteSequence}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteArrayByteSequenceTest extends OfficeFrameTestCase {

	/**
	 * Expected string.
	 */
	private final String TEST = "TEST";

	/**
	 * Input bytes.
	 */
	private final byte[] bytes = TEST.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link ByteArrayByteSequence} to test.
	 */
	private final ByteSequence sequence = new ByteArrayByteSequence(this.bytes);

	/**
	 * Ensure correct length.
	 */
	public void testCorrectLength() {
		assertEquals("Incorrect length", this.bytes.length, this.sequence.length());
	}

	/**
	 * Ensure can read contents.
	 */
	public void testReadContents() {
		byte[] content = new byte[this.sequence.length()];
		for (int i = 0; i < this.sequence.length(); i++) {
			assertEquals("Incorrect byte " + i, this.bytes[i], this.sequence.byteAt(i));
			content[i] = this.sequence.byteAt(i);
		}
		assertEquals("Incorrect reconstructed value", TEST, new String(content, ServerHttpConnection.HTTP_CHARSET));
	}
}
