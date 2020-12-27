/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;

/**
 * Tests the {@link StreamBufferByteSequence}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferByteSequenceTest {

	/**
	 * {@link ByteBuffer} size.
	 */
	private static final int BUFFER_SIZE = 4;

	/**
	 * {@link StreamBufferPool}
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool = new MockStreamBufferPool(
			() -> ByteBuffer.allocate(BUFFER_SIZE));

	/**
	 * {@link OutputStream} to write to the {@link StreamBuffer} instances.
	 */
	private BufferPoolServerOutputStream<ByteBuffer> output = new BufferPoolServerOutputStream<>(this.bufferPool);

	/**
	 * Ensure no data initially
	 */
	@Test
	public void empty() throws IOException {

		// Create with empty buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 0, 0);
		assertEquals(0, sequence.length(), "Should have no data");

		// Ensure index within lower range
		try {
			sequence.byteAt(-1);
		} catch (ArrayIndexOutOfBoundsException ex) {
			assertEquals("Asking for byte -1 of ByteSequence with length 0 bytes", ex.getMessage(),
					"Incorrect exception");
		}

		// Ensure index within upper range
		try {
			sequence.byteAt(0);
		} catch (ArrayIndexOutOfBoundsException ex) {
			assertEquals("Asking for byte 0 of ByteSequence with length 0 bytes", ex.getMessage(),
					"Incorrect exception");
		}
	}

	/**
	 * Ensure no data initially and then append some data.
	 */
	@Test
	public void emptyThenAppend() throws IOException {

		// Create with empty buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 0, 0);
		assertEquals(0, sequence.length(), "Should have no data");

		// Add no data then some data
		final int DATA_LENGTH = 10;
		for (int i = 0; i < DATA_LENGTH; i++) {

			// Add empty buffer
			StreamBuffer<ByteBuffer> emptyBuffer = this.bufferPool.getPooledStreamBuffer();
			sequence.appendStreamBuffer(emptyBuffer, 0, 0);

			// Add some data
			StreamBuffer<ByteBuffer> dataBuffer = this.bufferPool.getPooledStreamBuffer();
			dataBuffer.write((byte) (i + 1));
			sequence.appendStreamBuffer(dataBuffer, 0, BufferJvmFix.position(dataBuffer.pooledBuffer));
		}

		// Ensure able to obtain the data
		assertEquals(DATA_LENGTH, sequence.length(), "Incorrect number of bytes");
		for (int i = 0; i < DATA_LENGTH; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect byte");
		}
	}

	/**
	 * Ensure can load single {@link StreamBuffer} to read data.
	 */
	@Test
	public void singleStreamBuffer() throws IOException {

		// Add data for stream buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		buffer.write(new byte[] { 1, 2, 3, 4 });
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 0, BUFFER_SIZE);

		// Ensure can read the data
		assertEquals(4, sequence.length(), "Incorrect data length");
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect byte at " + i);
		}

		// Ensure index within upper range
		try {
			sequence.byteAt(BUFFER_SIZE + 1);
		} catch (ArrayIndexOutOfBoundsException ex) {
			assertEquals(
					"Asking for byte " + (BUFFER_SIZE + 1) + " of ByteSequence with length " + BUFFER_SIZE + " bytes",
					ex.getMessage(), "Incorrect exception");
		}
	}

	/**
	 * Ensure can load partial {@link StreamBuffer} to read data.
	 */
	@Test
	public void partialStreamBuffer() throws IOException {

		// Add data for stream buffer
		StreamBuffer<ByteBuffer> buffer = this.bufferPool.getPooledStreamBuffer();
		buffer.write(new byte[] { 1, 2, 3, 4 });
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 1, 2);

		// Ensure can read the data
		assertEquals(2, sequence.length(), "Incorrect data length");
		for (int i = 0; i < 2; i++) {
			assertEquals(i + 2, sequence.byteAt(i), "Incorrect byte at " + i);
		}
	}

	/**
	 * Ensure can load data from multiple {@link StreamBuffer} instances.
	 */
	@Test
	public void multipleStreamBuffers() throws IOException {

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
		StreamBuffer<ByteBuffer> buffer = output.getBuffers();
		while (buffer != null) {
			if (sequence == null) {
				sequence = new StreamBufferByteSequence(buffer, 0, BufferJvmFix.position(buffer.pooledBuffer));
			} else {
				sequence.appendStreamBuffer(buffer, 0, BufferJvmFix.position(buffer.pooledBuffer));
			}
			buffer = buffer.next;
		}

		// Ensure correct bytes
		assertEquals(expectedLength, sequence.length(), "Incorrect number of bytes");
		int expectedValue = Byte.MIN_VALUE;
		for (int i = 0; i < expectedLength; i++) {
			assertEquals(expectedValue++, sequence.byteAt(i), "Incorrect byte value at " + i);
		}

		// Ensure can use HTTP character sequence (data as is)
		CharSequence httpCharSequence = sequence.getHttpCharSequence();
		expectedValue = Byte.MIN_VALUE;
		for (int i = 0; i < expectedLength; i++) {
			assertEquals((expectedValue++) & 0xff, httpCharSequence.charAt(i), "Incorrect char value at " + i);
		}
	}

	/**
	 * Ensure can decode bytes to HTTP {@link String}.
	 */
	@Test
	public void toHttpString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("Content-Type",
				ServerHttpConnection.HTTP_CHARSET);
		assertEquals("Content-Type", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can trim HTTP {@link String}.
	 */
	@Test
	public void trimHttpString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence(" \t text/plain \t ",
				ServerHttpConnection.HTTP_CHARSET);
		sequence.trim();
		assertEquals("text/plain", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can trim a while {@link StreamBuffer}.
	 */
	@Test
	public void trimWholeStreamBuffer() throws IOException {

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
		assertEquals("TEST", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can trim {@link StreamBuffer} content of only spaces.
	 */
	@Test
	public void trimOnlySpaces() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("      ", ServerHttpConnection.HTTP_CHARSET);
		sequence.trim();
		assertEquals("", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can trim an empty {@link StreamBuffer}.
	 */
	@Test
	public void trimEmptyBuffer() throws IOException {
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(0));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
		StreamBufferByteSequence sequence = new StreamBufferByteSequence(buffer, 0, 0);
		sequence.trim();
		assertEquals("", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can trim a segmented {@link StreamBuffer}.
	 */
	@Test
	public void trimSegmentedBuffer() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("               T                 ",
				ServerHttpConnection.HTTP_CHARSET);
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(0));
		sequence.appendStreamBuffer(pool.getPooledStreamBuffer(), 0, 0);
		sequence.trim();
		assertEquals("T", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can remove quotes for HTTP {@link String}.
	 */
	@Test
	public void removeQuotesForHttpString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("\"text/plain\"",
				ServerHttpConnection.HTTP_CHARSET);
		sequence.removeQuotes(() -> new IOException("Should not occur"));
		assertEquals("text/plain", sequence.toHttpString(), "Incorrect HTTP content");
	}

	/**
	 * Ensure can remove quotes for HTTP {@link String} on buffer end.
	 */
	@Test
	public void removeQuotesOnBufferEnds() throws IOException {

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
		StreamBuffer<ByteBuffer> buffer = this.output.getBuffers();
		int size = 0;
		while (buffer != null) {
			size++;
			buffer = buffer.next;
		}
		assertEquals(3, size, "Incorrect number of buffers");

		// Ensure trims correctly
		sequence.trim();
		assertEquals("\"0123\"", sequence.toHttpString(), "Incorrect trimmed content");

		// Trim again (should do nothing) and remove quotes
		sequence.trim().removeQuotes(() -> new IOException("Should not occur"));
		assertEquals("0123", sequence.toHttpString(), "Incorrect unquoted content");
	}

	/**
	 * Ensure can remove quotes for invalid quoted value.
	 */
	@Test
	public void removeQuotesForInvalidQuotedValue() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("\"text/plain",
				ServerHttpConnection.HTTP_CHARSET);
		final Exception exception = new Exception("TEST");
		try {
			sequence.removeQuotes(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Incorrect exception");
		}
	}

	/**
	 * Ensure can decode all URI bytes.
	 */
	@Test
	public void uriDecodeAllBytes() throws IOException {

		final Function<Byte, Byte> getHex = (value) -> {
			if ((0 <= value) && (value <= 9)) {
				return (byte) (value + (byte) '0');
			} else if ((10 <= value) && (value < 16)) {
				return (byte) ((byte) 'A' + value - 10);
			} else {
				fail("Invalid value " + value);
				return null;
			}
		};

		final byte HTTP_PERCENTAGE = "%".getBytes(ServerHttpConnection.URI_CHARSET)[0];
		final byte HTTP_SPACE = " ".getBytes(ServerHttpConnection.URI_CHARSET)[0];

		// Write all bytes encoded
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
			byte hi = (byte) (i & 0xf0);
			hi >>= 4; // move hi to number
			hi = getHex.apply((byte) (hi & 0x0f));
			byte low = getHex.apply((byte) (i & 0x0f));

			// Write the encoded bytes
			this.output.write(HTTP_PERCENTAGE);
			this.output.write(hi);
			this.output.write(low);
		}

		// Add in the additional '+'
		StreamBufferByteSequence sequence = this.writeContentToSequence("+", ServerHttpConnection.URI_CHARSET);

		// Decode the sequence
		sequence.decodeUri((message) -> new IOException("Should not occur"));

		// Ensure the stream no has decoded bytes
		int index = 0;
		for (int value = Byte.MIN_VALUE; value <= Byte.MAX_VALUE; value++) {
			assertEquals(value, sequence.byteAt(index++), "Incorrect decoded byte at " + index);
		}
		assertEquals(HTTP_SPACE, sequence.byteAt(index++), "Incorrect decoded +");
	}

	/**
	 * Ensure handles invalid URI encoding.
	 */
	@Test
	public void invalidUriEncoding() throws IOException {
		final Exception exception = new Exception("TEST");
		StreamBufferByteSequence sequence = this.writeContentToSequence("%G@", ServerHttpConnection.URI_CHARSET);
		try {
			sequence.decodeUri((message) -> exception);
		} catch (Exception ex) {
			assertSame(exception, ex, "Incorrect exception");
		}
	}

	/**
	 * Ensure handles incomplete URI encoding.
	 */
	@Test
	public void incompleteUriEncoding() throws IOException {
		final Exception exception = new Exception("TEST");
		StreamBufferByteSequence sequence = this.writeContentToSequence("%A", ServerHttpConnection.URI_CHARSET);
		try {
			sequence.decodeUri((message) -> exception);
		} catch (Exception ex) {
			assertSame(exception, ex, "Incorrect exception");
		}
	}

	/**
	 * Ensure can decode bytes along with URI decoding.
	 */
	@Test
	public void toUriString() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("Test%20Decode+",
				ServerHttpConnection.URI_CHARSET);
		assertEquals(
				"Test Decode ", sequence.decodeUri((message) -> new IOException("Should not occur: " + message))
						.toUriString((result) -> new IOException("Should not occur: " + result)),
				"Incorrect decoded URI");
	}

	/**
	 * Ensure can decode bytes to {@link Charset}.
	 */
	@Test
	public void toStringCharset() throws IOException {
		Charset charset = Charset.forName("UTF-16");
		StreamBufferByteSequence sequence = this.writeContentToSequence("This should be encoded in UTF-16", charset);
		assertEquals("This should be encoded in UTF-16",
				sequence.toString(charset, (result) -> new IOException("Should not occur")), "Incorrect decoded text");
	}

	/**
	 * Ensure can obtain long value.
	 */
	@Test
	public void toLong() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("9876543210",
				ServerHttpConnection.HTTP_CHARSET);
		assertEquals(9876543210L, sequence.toLong((character) -> new IOException("Should not occur")),
				"Incorrect long value");
	}

	/**
	 * Ensure handle invalid long.
	 */
	@Test
	public void invalidLong() throws IOException {
		StreamBufferByteSequence sequence = this.writeContentToSequence("invalid", ServerHttpConnection.HTTP_CHARSET);
		Exception exception = new Exception("TEST");
		try {
			sequence.toLong((character) -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Incorrect cause");
		}
	}

	/**
	 * Writes the content to the {@link StreamBufferByteSequence}.
	 * 
	 * @param content Content to write.
	 * @param charset {@link Charset}.
	 * @return {@link StreamBufferByteSequence} containing the byte content.
	 */
	private StreamBufferByteSequence writeContentToSequence(String content, Charset charset) throws IOException {

		// Write out all the byte values (encoded)
		OutputStreamWriter writer = new OutputStreamWriter(this.output, charset);
		writer.write(content);
		writer.flush();

		// Load the byte sequence
		StreamBufferByteSequence sequence = null;
		StreamBuffer<ByteBuffer> buffer = this.output.getBuffers();
		while (buffer != null) {
			if (sequence == null) {
				sequence = new StreamBufferByteSequence(buffer, 0, BufferJvmFix.position(buffer.pooledBuffer));
			} else {
				sequence.appendStreamBuffer(buffer, 0, BufferJvmFix.position(buffer.pooledBuffer));
			}
			buffer = buffer.next;
		}

		// Return the byte sequence
		return sequence;
	}

}
