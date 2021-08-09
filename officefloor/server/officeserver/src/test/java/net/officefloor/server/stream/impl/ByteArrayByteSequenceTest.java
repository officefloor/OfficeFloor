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
