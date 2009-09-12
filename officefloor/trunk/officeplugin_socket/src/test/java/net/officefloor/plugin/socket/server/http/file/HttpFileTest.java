/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.socket.server.http.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link HttpFile}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileTest extends OfficeFrameTestCase {

	/**
	 * Ensure can serialise {@link java.nio.HeapByteBuffer} containing the
	 * contents.
	 */
	public void testSerialiseWithHeapByteBuffer() throws Exception {

		// Create the contents
		ByteBuffer heapBuffer = ByteBuffer.allocate(10);
		byte[] contents = this.createContents(heapBuffer);
		assertTrue("Must be heap buffer for valid test", heapBuffer.hasArray());

		// Create Http File (with mock details)
		HttpFile httpFile = new HttpFileImpl("/path", "zip",
				"text/html; charset=UTF-8", heapBuffer);
		assertTrue("Should exist for valid test", httpFile.isExist());

		// Ensure can serialise
		this.doSerialiseTest(httpFile, "/path", true, "zip",
				"text/html; charset=UTF-8", contents);
	}

	/**
	 * Ensure can sertialise {@link java.nio.HeapByteBuffer} with offset.
	 */
	public void testSerialiseWithHeapByteBufferOffset() throws Exception {

		// Create the large contents
		ByteBuffer largeHeapBuffer = ByteBuffer.allocate(100);
		byte[] largeContents = this.createContents(largeHeapBuffer);

		// Create byte buffer with offset
		largeHeapBuffer.position(10);
		largeHeapBuffer.limit(20);
		ByteBuffer offsetHeapBuffer = largeHeapBuffer.slice();
		byte[] contents = Arrays.copyOfRange(largeContents, 10, 20);

		// Validate that a slice
		assertTrue("Must be heap buffer for valid test", offsetHeapBuffer
				.hasArray());
		assertEquals("Incorrect offset", 10, offsetHeapBuffer.arrayOffset());
		assertEquals("incorrect limit", 10, offsetHeapBuffer.limit());

		// Create Http File (with mock details)
		HttpFile httpFile = new HttpFileImpl("/path", "",
				"application/octet-stream", offsetHeapBuffer);

		// Ensure can serialise
		this.doSerialiseTest(httpFile, "/path", true, "",
				"application/octet-stream", contents);
	}

	/**
	 * Ensure can serialise {@link java.nio.DirectByteBuffer} containing the
	 * contents.
	 */
	public void testSerialiseWithDirectByteBuffer() throws Exception {

		// Create the contents
		ByteBuffer heapBuffer = ByteBuffer.allocateDirect(10);
		byte[] contents = this.createContents(heapBuffer);
		assertFalse("Must be direct buffer for valid test", heapBuffer
				.hasArray());

		// Create Http File (with mock details)
		HttpFile httpFile = new HttpFileImpl("/path", null, null, heapBuffer);

		// Ensure can serialise
		this.doSerialiseTest(httpFile, "/path", true, "", "", contents);
	}

	/**
	 * Ensure can serialise a {@link HttpFile} that does not exist.
	 */
	public void testSerialiseNotExistingHttpFile() throws Exception {

		// Create the Http File (not existing)
		HttpFile httpFile = new HttpFileImpl("/path");
		assertFalse("Should not exist for valid test", httpFile.isExist());

		// Ensure can serialise
		this.doSerialiseTest(httpFile, "/path", false, "", "", new byte[0]);
	}

	/**
	 * Ensure equals is based on details of {@link HttpFile}.
	 */
	public void testEquals() {
		HttpFile one = new HttpFileImpl("/path", "encoding", "type", null);
		HttpFile two = new HttpFileImpl("/path", "encoding", "type", null);
		assertEquals("Should equal", one, two);
		assertEquals("Same hash", one.hashCode(), two.hashCode());
	}

	/**
	 * Ensures not equals should details differ.
	 */
	public void testNotEquals() {
		final String PATH = "/path";
		final String ENCODING = "encoding";
		final String TYPE = "type";
		HttpFile file = new HttpFileImpl(PATH, ENCODING, TYPE, null);
		assertFalse("Should not match if different path", file
				.equals(new HttpFileImpl("/wrong/path", ENCODING, TYPE, null)));
		assertFalse("Should not match if different encoding", file
				.equals(new HttpFileImpl(PATH, "wrong", TYPE, null)));
		assertFalse("Should not match if different type", file
				.equals(new HttpFileImpl(PATH, ENCODING, "wrong", null)));
	}

	/**
	 * Ensure obtain details from <code>toString</code> method.
	 */
	public void testToString() {
		HttpFile file = new HttpFileImpl("/path", "encoding", "type", null);
		assertEquals(
				"Incorrect toString",
				"HttpFileImpl: /path (Exist: true, Content-Encoding: encoding, Content-Type: type)",
				file.toString());
	}

	/**
	 * Create content for testing.
	 *
	 * @param buffer
	 *            {@link ByteBuffer} to fill with content.
	 * @return Content filled into the {@link ByteBuffer}.
	 */
	private byte[] createContents(ByteBuffer buffer) {
		byte[] contents = new byte[buffer.remaining()];
		for (int i = 0; i < contents.length; i++) {
			contents[i] = (byte) i;
		}
		buffer.put(contents);
		buffer.flip(); // make ready for use
		return contents;
	}

	/**
	 * Does the serialise testing.
	 *
	 * @param httpFile
	 *            {@link HttpFile} to serialise test.
	 * @param path
	 *            Expected path.
	 * @param isExist
	 *            Flag indicating if expected to exist.
	 * @param contentEncoding
	 *            Expected content encoding.
	 * @param contentType
	 *            Expected content type.
	 * @param contents
	 *            Expected contents.
	 */
	private void doSerialiseTest(HttpFile httpFile, String path,
			boolean isExist, String contentEncoding, String contentType,
			byte[] contents) throws Exception {

		// Serialise the http file
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(data);
		outputStream.writeObject(httpFile);
		outputStream.flush();

		// Retrieve the http file
		ObjectInputStream inputStream = new ObjectInputStream(
				new ByteArrayInputStream(data.toByteArray()));
		HttpFile retrievedFile = (HttpFile) inputStream.readObject();

		// Should be a new http file object
		assertNotSame("Should be new HTTP File object", httpFile, retrievedFile);

		// Validate the retrieved file
		assertEquals("Incorrect path", path, retrievedFile.getPath());
		assertEquals("Incorrect existence", isExist, retrievedFile.isExist());
		assertEquals("Incorrect content-encoding", contentEncoding,
				retrievedFile.getContentEncoding());
		assertEquals("Incorrect content-type", contentType, retrievedFile
				.getContentType());
		ByteBuffer buffer = retrievedFile.getContents();
		assertEquals("Incorrect content position", 0, buffer.position());
		assertEquals("Incorrect content size", contents.length, buffer
				.remaining());
		for (int i = 0; i < contents.length; i++) {
			byte contentByte = contents[i];
			byte bufferByte = buffer.get();
			assertEquals("Incorrect byte at index " + i, contentByte,
					bufferByte);
		}

		// Ensure byte buffer positions are valid
		assertEquals("Should not change original byte buffer position", 0,
				httpFile.getContents().position());
	}

}