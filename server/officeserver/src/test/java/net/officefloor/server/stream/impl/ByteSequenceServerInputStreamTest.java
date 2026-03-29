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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerInputStream;

/**
 * Tests the {@link ByteSequenceServerInputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteSequenceServerInputStreamTest extends OfficeFrameTestCase {

	/**
	 * Test string.
	 */
	private final String TEST_test = "TEST_test";

	/**
	 * Bytes.
	 */
	private final byte[] bytes = TEST_test.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link ByteSequenceServerInputStream} to test.
	 */
	private final ByteSequenceServerInputStream inputStream = new ByteSequenceServerInputStream(
			new ByteArrayByteSequence(this.bytes), 0);

	/**
	 * Ensure all bytes available.
	 */
	public void testAvailable() throws IOException {
		assertEquals("Incorrect number available", this.bytes.length, this.inputStream.available());
	}

	/**
	 * Ensure can read in the bytes.
	 */
	public void testReadBytes() throws IOException {
		byte[] data = new byte[this.inputStream.available()];
		for (int i = 0; i < data.length; i++) {
			assertEquals("Ensure decrementing available left", (data.length - i), this.inputStream.available());
			data[i] = (byte) this.inputStream.read();
			assertEquals("Incorrect byte value " + i, this.bytes[i], data[i]);
		}
		assertEquals("Should now be end of stream", -1, this.inputStream.read());
	}

	/**
	 * Ensure can bulk read bytes.
	 */
	public void testBulkReadAllBytes() throws IOException {
		byte[] data = new byte[this.inputStream.available()];
		int bytesRead = this.inputStream.read(data);
		assertEquals("Should read all bytes", data.length, bytesRead);
		assertEquals("Should be end of stream", -1, this.inputStream.read());
		assertEquals("Incorrect read bytes", TEST_test, new String(data, ServerHttpConnection.HTTP_CHARSET));
	}

	/**
	 * Ensure can bulk read partial bytes.
	 */
	public void testBulkReadPartialBytes() throws IOException {
		byte[] data = new byte[12];
		final int offset = 4;
		final int length = 5;
		int bytesRead = this.inputStream.read(data, offset, length);
		assertEquals("Incorrect number of bytes read", length, bytesRead);
		assertEquals("Incorrect read bytes", "TEST_",
				new String(data, offset, length, ServerHttpConnection.HTTP_CHARSET));
	}

	/**
	 * Ensure can browse rest of {@link ServerInputStream}.
	 */
	public void testBrowseStream() throws IOException {

		// Read the first 4 bytes
		final String TEST = "TEST";
		byte[] data = new byte[TEST.length()];
		assertEquals("Ensure TEST read", data.length, this.inputStream.read(data));
		assertEquals("Incorrect value", TEST, new String(data, ServerHttpConnection.HTTP_CHARSET));
		assertEquals("Incorrect read value", "_".getBytes(ServerHttpConnection.HTTP_CHARSET)[0],
				this.inputStream.read());

		// Ensure can browse rest of string
		int offset = data.length + 1;
		InputStream browseStream = this.inputStream.createBrowseInputStream();
		byte[] browse = new byte[this.inputStream.available()];
		for (int i = 0; i < browse.length; i++) {
			browse[i] = (byte) browseStream.read();
			assertEquals("Incorrect browse byte " + i, this.bytes[offset + i], browse[i]);
		}
		assertEquals("Inccorect browsed text", "test", new String(browse, ServerHttpConnection.HTTP_CHARSET));

		// Ensure can read further data
		byte[] further = new byte[2];
		assertEquals("Incorrect number of bytes read", further.length, this.inputStream.read(further));
		assertEquals("Incorrect further data read", "te", new String(further, ServerHttpConnection.HTTP_CHARSET));

		// Ensure can browse after further reading
		offset += further.length;
		browseStream = this.inputStream.createBrowseInputStream();
		browse = new byte[this.inputStream.available()];
		assertEquals("Incorrect number of bytes read", browse.length, this.inputStream.read(browse));
		assertEquals("Incorrect browsed content", "st", new String(browse, ServerHttpConnection.HTTP_CHARSET));

		// Read rest of string
		this.inputStream.read();
		this.inputStream.read();
		assertEquals("Should be end of input", -1, this.inputStream.read());
		assertEquals("Should continue to be end of input", -1, this.inputStream.read());
		assertEquals("Should also be end of browse stream", -1, this.inputStream.createBrowseInputStream().read());
	}

}
