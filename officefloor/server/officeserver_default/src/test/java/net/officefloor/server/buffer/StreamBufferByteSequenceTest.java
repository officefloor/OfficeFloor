/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;

/**
 * Tests the {@link StreamBufferByteSequence}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferByteSequenceTest extends OfficeFrameTestCase {

	/**
	 * {@link ByteBuffer} size.
	 */
	private static final int BUFFER_SIZE = 4;

	/**
	 * {@link BufferPool}
	 */
	private final BufferPool<ByteBuffer> bufferPool = new MockBufferPool(() -> ByteBuffer.allocate(BUFFER_SIZE));

	/**
	 * {@link OutputStream} to write to the {@link StreamBuffer} instances.
	 */
	private BufferPoolServerOutputStream<ByteBuffer> output = new BufferPoolServerOutputStream<>(this.bufferPool);

	/**
	 * Ensure no data initially
	 */
	public void testEmpty() {

		// Add empty buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 0, 0);
		assertEquals("Should have no data", 0, sequence.length());

		// Ensure index within lower range
		try {
			sequence.byteAt(-1);
		} catch (ArrayIndexOutOfBoundsException ex) {
			assertEquals("Incorrect exception", "Asking for byte -1 of ByteSequence with length 0 bytes",
					ex.getMessage());
		}

		// Ensure index within upper range
		try {
			sequence.byteAt(0);
		} catch (ArrayIndexOutOfBoundsException ex) {
			assertEquals("Incorrect exception", "Asking for byte 0 of ByteSequence with length 0 bytes",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can load single {@link StreamBuffer} to read data.
	 */
	public void testSingleStreamBuffer() {

		// Add data for stream buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		buffer.write(new byte[] { 1, 2, 3, 4 });
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 0, BUFFER_SIZE);

		// Ensure can read the data
		assertEquals("Incorrect data length", 4, sequence.length());
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect byte at " + i, i + 1, sequence.byteAt(i));
		}

		// Ensure index within upper range
		try {
			sequence.byteAt(BUFFER_SIZE + 1);
		} catch (ArrayIndexOutOfBoundsException ex) {
			assertEquals("Incorrect exception",
					"Asking for byte " + (BUFFER_SIZE + 1) + " of ByteSequence with length " + BUFFER_SIZE + " bytes",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can load partial {@link StreamBuffer} to read data.
	 */
	public void testPartialStreamBuffer() {

		// Add data for stream buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		buffer.write(new byte[] { 1, 2, 3, 4 });
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 1, 2);

		// Ensure can read the data
		assertEquals("Incorrect data length", 2, sequence.length());
		for (int i = 0; i < 2; i++) {
			assertEquals("Incorrect byte at " + i, i + 2, sequence.byteAt(i));
		}
	}

	/**
	 * Ensure can load data from multiple {@link StreamBuffer} instances.
	 */
	public void testMultipleStreamBuffers() throws IOException {

		// Write out all the byte values
		@SuppressWarnings("resource")
		BufferPoolServerOutputStream<ByteBuffer> output = new BufferPoolServerOutputStream<>(this.bufferPool);
		int expectedLength = 0;
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
			output.write((byte) i);
			expectedLength++;
		}

		// Load up byte sequence
		StreamBufferByteSequence sequence = null;
		for (StreamBuffer<ByteBuffer> buffer : output.getBuffers()) {
			if (sequence == null) {
				sequence = new StreamBufferByteSequence(buffer, 0, buffer.getPooledBuffer().position());
			} else {
				sequence.appendStreamBuffer(buffer, 0, buffer.getPooledBuffer().position());
			}
		}

		// Ensure correct bytes
		assertEquals("Incorrect number of bytes", expectedLength, sequence.length());
		int expectedValue = Byte.MIN_VALUE;
		for (int i = 0; i < expectedLength; i++) {
			assertEquals("Incorrect byte value at " + i, expectedValue++, sequence.byteAt(i));
		}

		// Ensure can use HTTP character sequence (data as is)
		CharSequence httpCharSequence = sequence.getHttpCharSequence();
		expectedValue = Byte.MIN_VALUE;
		for (int i = 0; i < expectedLength; i++) {
			assertEquals("Incorrect char value at " + i, (expectedValue++) & 0xff, httpCharSequence.charAt(i));
		}
	}

	/**
	 * Ensure can decode bytes to HTTP {@link String}.
	 */
	public void testToHttpString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("Content-Type",
				ServerHttpConnection.HTTP_CHARSET);
		assertEquals("Incorrect HTTP content", "Content-Type", sequence.toHttpString());
	}

	/**
	 * Ensure can trim HTTP {@link String}.
	 */
	public void testTrimHttpString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence(" \t text/plain \t ",
				ServerHttpConnection.HTTP_CHARSET);
		sequence.trim();
		assertEquals("Incorrect HTTP content", "text/plain", sequence.toHttpString());
	}

	/**
	 * Ensure can trim a while {@link StreamBuffer}.
	 */
	public void testTrimWholeStreamBuffer() throws IOException {

		// Write the content (with spacing filling a whole buffer either side)
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < BUFFER_SIZE + 1; i++) {
			content.append(' ');
		}
		content.append("TEST");
		for (int i = 0; i < BUFFER_SIZE + 1; i++) {
			content.append(' ');
		}
		StreamBufferByteSequence sequence = this.writeContentToSequence(content.toString(),
				ServerHttpConnection.HTTP_CHARSET);

		// Trim
		sequence.trim();
		assertEquals("Incorrect HTTP content", "TEST", sequence.toHttpString());
	}

	/**
	 * Ensure can trim {@link StreamBuffer} content of only spaces.
	 */
	public void testTrimOnlySpaces() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("      ", ServerHttpConnection.HTTP_CHARSET);
		sequence.trim();
		assertEquals("Incorrect HTTP content", "", sequence.toHttpString());
	}

	/**
	 * Ensure can remove quotes for HTTP {@link String}.
	 */
	public void testRemoveQuotesForHttpString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("\"text/plain\"",
				ServerHttpConnection.HTTP_CHARSET);
		sequence.removeQuotes(() -> new IOException("Should not occur"));
		assertEquals("Incorrect HTTP content", "text/plain", sequence.toHttpString());
	}

	/**
	 * Ensure can remove quotes for HTTP {@link String} on buffer end.
	 */
	public void testRemoveQuotesOnBufferEnds() throws IOException {

		// Write content with quotes on buffer edges
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < BUFFER_SIZE - 1; i++) {
			content.append(' ');
		}
		content.append('"');
		for (int i = 0; i < BUFFER_SIZE; i++) {
			content.append(i);
		}
		content.append('"');
		for (int i = 1; i < BUFFER_SIZE; i++) {
			content.append('\t');
		}
		StreamBufferByteSequence sequence = this.writeContentToSequence(content.toString(),
				ServerHttpConnection.HTTP_CHARSET);

		// Ensure using three buffers
		assertEquals("Incorrect number of buffers", 3, this.output.getBuffers().size());

		// Ensure trims correctly
		sequence.trim();
		assertEquals("Incorrect trimmed content", "\"0123\"", sequence.toHttpString());

		// Trim again (should do nothing) and remove quotes
		sequence.trim().removeQuotes(() -> new IOException("Should not occur"));
		assertEquals("Incorrect unquoted content", "0123", sequence.toHttpString());
	}

	/**
	 * Ensure can remove quotes for invalid quoted value.
	 */
	public void testRemoveQuotesForInvalidQuotedValue() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("\"text/plain",
				ServerHttpConnection.HTTP_CHARSET);
		final Exception exception = new Exception("TEST");
		try {
			sequence.removeQuotes(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}
	
	/**
	 * Ensure can decode bytes along with URI decoding.
	 */
	public void testToUriString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("Test%20Decode",
				ServerHttpConnection.URI_CHARSET);
		assertEquals("Incorrect decoded URI", "Test Decode", sequence.toUriString());
	}

	/**
	 * Ensure can decode bytes to {@link Charset}.
	 */
	public void testToStringCharset() throws IOException {
		Charset charset = Charset.forName("UTF-16");
		StreamBufferByteSequence sequence = this.writeContentToSequence("This should be encoded in UTF-16", charset);
		assertEquals("Incorrect decoded text", "This should be encoded in UTF-16",
				sequence.toString(charset, (result) -> new IOException("Should not occur")));
	}

	/**
	 * Writes the content to the {@link StreamBufferByteSequence}.
	 * 
	 * @param content
	 *            Content to write.
	 * @param charset
	 *            {@link Charset}.
	 * @return {@link StreamBufferByteSequence} containing the byte content.
	 */
	private StreamBufferByteSequence writeContentToSequence(String content, Charset charset) throws IOException {

		// Write out all the byte values (encoded)
		OutputStreamWriter writer = new OutputStreamWriter(this.output, charset);
		writer.write(content);
		writer.flush();

		// Load the byte sequence
		StreamBufferByteSequence sequence = null;
		for (StreamBuffer<ByteBuffer> buffer : this.output.getBuffers()) {
			if (sequence == null) {
				sequence = new StreamBufferByteSequence(buffer, 0, buffer.getPooledBuffer().position());
			} else {
				sequence.appendStreamBuffer(buffer, 0, buffer.getPooledBuffer().position());
			}
		}

		// Return the byte sequence
		return sequence;
	}

}