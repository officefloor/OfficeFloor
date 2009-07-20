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
package net.officefloor.plugin.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link BufferStream}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractBufferStreamTest extends OfficeFrameTestCase {

	/**
	 * {@link BufferStream} to test.
	 */
	private final BufferStream bufferStream = this.createBufferStream();

	/**
	 * Creates the {@link BufferStream} to test.
	 *
	 * @return {@link BufferStream} to test.
	 */
	protected abstract BufferStream createBufferStream();

	/**
	 * <p>
	 * Obtains the size of the {@link ByteBuffer} instances within the
	 * {@link BufferStream} being tested.
	 * <p>
	 * This allows tests to create content that span multiple buffers.
	 *
	 * @return Size of the underlying {@link ByteBuffer} instances.
	 */
	protected abstract int getBufferSize();

	/**
	 * {@link InputBufferStream}.
	 */
	private final InputBufferStream input = this.bufferStream
			.getInputBufferStream();

	/**
	 * {@link InputStream}.
	 */
	private final InputStream inputStream = this.input.getInputStream();

	/**
	 * {@link OutputBufferStream}.
	 */
	private final OutputBufferStream output = this.bufferStream
			.getOutputBufferStream();

	/**
	 * {@link OutputStream}.
	 */
	private final OutputStream outputStream = this.output.getOutputStream();

	/*
	 * ================ OutputStream / InputStream tests =====================
	 */

	/**
	 * Ensure able to output and input a byte via streams.
	 */
	public void testStream_OutputInputByte() throws IOException {
		final char VALUE = 'a';
		this.outputStream.write(VALUE);
		int value = this.inputStream.read();
		assertEquals("Incorrect result", VALUE, value);
		this.inputStream.close();
	}

	/**
	 * Ensure issue if attempting to read when no data is available.
	 */
	public void testStream_NoDataOnBlockingRead() throws IOException {
		try {
			this.inputStream.read();
			fail("Should not read data");
		} catch (NoBufferStreamContentException ex) {
			// Correct exception
		}
	}

	/**
	 * Ensure issue if attempting to read when no data is available.
	 */
	public void testStream_NoDataOnBlockingBufferedRead() throws IOException {
		byte[] result = new byte[1];
		try {
			this.inputStream.read(result);
			fail("Should not read data");
		} catch (NoBufferStreamContentException ex) {
			// Correct exception
		}
	}

	/**
	 * Ensure able to output, input, output and input a byte via streams.
	 */
	public void testStream_OutputInputOutputInputByte() throws IOException {
		final char FIRST = 'a';
		this.outputStream.write(FIRST);
		int first = this.inputStream.read();
		assertEquals("Incorrect result", FIRST, first);
		final char SECOND = 'b';
		this.outputStream.write(SECOND);
		int second = this.inputStream.read();
		assertEquals("Incorrect result", SECOND, second);
		this.inputStream.close();
	}

	/**
	 * Ensure able to write very large content and read the very large content.
	 */
	public void testStream_OutputInputLargeContent() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 100);

		// Write the content
		this.outputStream.write(content);

		// Read the content
		byte[] result = new byte[content.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) this.inputStream.read();
		}

		// Ensure correct result
		assertEquals(content, result);

		// Ensure closed
		this.inputStream.close();
	}

	/**
	 * Ensure able to input large content in one read. Content will come from
	 * multiple underlying buffers.
	 */
	public void testStream_InputLargeContent() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 100);

		// Write the content
		this.outputStream.write(content);

		// Read the content in one read
		byte[] result = new byte[content.length];
		int readSize = this.inputStream.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);

		// Ensure correct result
		assertEquals(content, result);

		// Ensure closed
		this.inputStream.close();
	}

	/**
	 * Ensure able to write buffer and read buffer via stream.
	 */
	public void testStream_OutputInputBuffer() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 2);

		// Write the content
		this.outputStream.write(content);

		// Read the content
		byte[] result = new byte[content.length];
		int readSize = this.inputStream.read(result);
		assertEquals("Incorrect bytes read", content.length, readSize);

		// Ensure correct result
		assertEquals(content, result);

		// Ensure closed
		this.inputStream.close();
	}

	/**
	 * Ensure indicates if end of stream (read all data after output stream
	 * closed).
	 */
	public void testStream_EndOfStream() throws IOException {

		// Write a byte and then close output
		this.outputStream.write('a');
		this.outputStream.close();

		// Read first byte and then should always indicate closed
		assertEquals("Incorrect read byte", 'a', this.inputStream.read());
		assertEquals("Should now be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());

		// Verify that from now on indicates that closed
		for (int i = 0; i < 1000; i++) {
			assertEquals("Should always be end of stream",
					BufferStream.END_OF_STREAM, this.inputStream.read());
		}
	}

	/**
	 * Ensure fail to write if output stream closed.
	 */
	public void testStream_OutputClosed() throws IOException {

		// Write byte to stream and then close output
		this.outputStream.write('a');
		this.outputStream.close();

		// Ensure that can not write further content
		try {
			this.outputStream.write('b');
			fail("Should not successfully write");
		} catch (ClosedChannelException ex) {
			// Correct exception
		}

		// Ensure can read content before close
		assertEquals("Incorrect read byte", 'a', this.inputStream.read());
		assertEquals("Should now be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());
	}

	/**
	 * Ensure fail to read if input stream closed.
	 */
	public void testStream_InputClosed() throws IOException {

		// Write two bytes to ensure content
		this.outputStream.write('a');
		this.outputStream.write('b');

		// Read first byte and then close input
		int result = this.inputStream.read();
		assertEquals("Incorrect read byte", 'a', result);
		this.inputStream.close();

		// Ensure stream closed for input
		try {
			this.inputStream.read();
			fail("Should not read successfully if closed input");
		} catch (ClosedChannelException ex) {
			// Correct exception
		}
	}

	/*
	 * ==================== Bytes tests ======================================
	 */

	/**
	 * Ensure able to output and input bytes.
	 */
	public void testBytes_OutputInputBytes() throws IOException {

		// Create the content
		byte[] content = this.createContent(this.getBufferSize());

		// Write the bytes
		this.output.write(content);

		// Read the bytes
		byte[] result = new byte[content.length];
		int readSize = this.input.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);

		// Ensure read results are correct
		assertEquals(content, result);

		// Close the stream
		this.input.close();
	}

	/**
	 * Ensure able to output and input large byte content.
	 */
	public void testBytes_OutputInputLargeContent() throws IOException {

		// Create the content
		byte[] content = this.createContent(this.getBufferSize() * 100);

		// Write the bytes
		this.output.write(content);

		// Read the bytes
		byte[] result = new byte[content.length];
		int readSize = this.input.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);

		// Ensure read results are correct
		assertEquals(content, result);

		// Close the stream
		this.input.close();
	}

	/*
	 * ==================== Buffer tests ======================================
	 */

	/**
	 * Ensure able to append {@link ByteBuffer}.
	 */
	public void testBuffer_AppendBuffer() throws IOException {

		// Create buffer to append
		byte[] content = this.createContent(this.getBufferSize() * 2);
		ByteBuffer contentBuffer = ByteBuffer.wrap(content);

		// Append the byte buffer
		this.output.append(contentBuffer);

		// Read the bytes
		byte[] result = new byte[content.length];
		int readSize = this.input.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);

		// Ensure read results are correct
		assertEquals(content, result);

		// All squirts should be closed
	}

	/**
	 * Ensure able to read {@link ByteBuffer}.
	 */
	public void testBuffer_ReadBuffer() throws IOException {

		// Write content to the buffer
		byte[] content = this.createContent(this.getBufferSize());
		this.outputStream.write(content);

		// Read the byte buffer
		CompareBufferProcessor.assertRead(this.input, content);

		// Ensure data read and no further data available
		CompareBufferProcessor.assertRead(this.input, null);

		// Ensure verify end of stream
		this.output.close();
		CompareBufferProcessor.assertEndOfStream(this.input);

		// All squirts should be closed
	}

	/**
	 * Ensure able to partially read the {@link ByteBuffer}.
	 */
	public void testBuffer_PartialReadBuffer() throws IOException {

		// Write content to the buffer
		byte[] contentA = this.createContent(this.getBufferSize() / 3);
		byte[] contentB = this.createContent(this.getBufferSize() / 3);
		this.outputStream.write(contentA);
		this.outputStream.write(contentB);

		// Read the buffer contents in partial chunks
		CompareBufferProcessor.assertRead(this.input, contentA);

		// Ensure on output close that can continue reading remaining
		this.output.close();
		CompareBufferProcessor.assertRead(this.input, contentB);

		// Ensure now end of stream
		CompareBufferProcessor.assertEndOfStream(this.input);

		// All squirts should be closed
	}

	/**
	 * Ensure that appended {@link ByteBuffer} is same read {@link ByteBuffer}.
	 */
	public void testBuffer_SameBuffer() throws IOException,
			BufferProcessException {

		// Create buffer to append
		byte[] content = this.createContent(10);
		ByteBuffer contentBuffer = ByteBuffer.wrap(content);

		// Append the buffer
		this.output.append(contentBuffer);

		// Read the buffer
		final ByteBuffer[] processBuffer = new ByteBuffer[1];
		this.input.read(new BufferProcessor() {
			@Override
			public void process(ByteBuffer buffer) throws Exception {
				processBuffer[0] = buffer;
			}
		});

		// Validate buffer (not same buffer as duplicate)
		byte[] result = new byte[content.length];
		processBuffer[0].get(result);
		assertEquals(content, result);
	}

	/*
	 * ==================== Join Stream tests ================================
	 */

	/**
	 * Ensure able to stream content from one {@link BufferStream} to another
	 * {@link BufferStream}.
	 */
	public void testJoin() throws IOException {

		// Populate the buffer stream
		byte[] content = this.createContent(this.getBufferSize());
		this.outputStream.write(content);

		// Create another buffer stream
		BufferStream joinStream = this.createBufferStream();
		OutputBufferStream joinOutput = joinStream.getOutputBufferStream();

		// Read contents from buffer stream into other buffer stream
		int readSize = this.input.read(joinOutput);
		assertEquals("Incorrect transfer size of bytes", content.length,
				readSize);

		// Validate no further data in original buffer stream
		this.outputStream.close();
		assertEquals("No further data for original",
				BufferStream.END_OF_STREAM, this.inputStream.read());

		// Validate data transferred to other buffer stream
		InputBufferStream joinInput = joinStream.getInputBufferStream();
		byte[] result = new byte[content.length];
		readSize = joinInput.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);
		assertEquals(content, result);

		// Close stream
		joinInput.close();
	}

	/*
	 * ==================== Combo tests ======================================
	 */

	/**
	 * Ensure able to mix outputting via streams and appending buffers.
	 * {@link ByteBuffer} appended first.
	 */
	public void testCombo_MixAppendOutputBuffer() throws IOException {

		// Create contents
		byte[] contentA = this.createContent(this.getBufferSize() * 2);
		byte[] contentB = this.createContent(this.getBufferSize() * 2);
		byte[] contentC = this.createContent(this.getBufferSize() * 2);
		byte[] contentD = this.createContent(this.getBufferSize() * 2);

		// Write and append the contents
		this.output.append(ByteBuffer.wrap(contentA));
		this.outputStream.write(contentB);
		this.output.append(ByteBuffer.wrap(contentC));
		this.outputStream.write(contentD);

		// Close the output
		this.output.close();

		// Read the results
		this.assertInputStreamRead(contentA);
		this.assertInputStreamRead(contentB);
		this.assertInputStreamRead(contentC);
		this.assertInputStreamRead(contentD);

		// Ensure end of input stream
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());

		// Close the stream
		this.input.close();
	}

	/**
	 * Ensure able to mix outputting via streams and appending buffers. Bytes
	 * written first.
	 */
	public void testCombo_MixOutputAppendBuffer() throws IOException {

		// Create contents
		byte[] contentA = this.createContent(this.getBufferSize() * 2);
		byte[] contentB = this.createContent(this.getBufferSize() * 2);
		byte[] contentC = this.createContent(this.getBufferSize() * 2);
		byte[] contentD = this.createContent(this.getBufferSize() * 2);

		// Write and append the contents
		this.outputStream.write(contentA);
		this.output.append(ByteBuffer.wrap(contentB));
		this.outputStream.write(contentC);
		this.output.append(ByteBuffer.wrap(contentD));

		// Close the output
		this.output.close();

		// Read the results
		this.assertInputStreamRead(contentA);
		this.assertInputStreamRead(contentB);
		this.assertInputStreamRead(contentC);
		this.assertInputStreamRead(contentD);

		// Ensure end of input stream
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());

		// Close the stream
		this.input.close();
	}

	/**
	 * Ensure able to mix reading bytes and buffers. Bytes read first.
	 */
	public void testCombo_MixReadBytesBuffer() throws IOException {

		// Create contents
		byte[] contentA = this.createContent(this.getBufferSize());
		byte[] contentB = this.createContent(this.getBufferSize());
		byte[] contentC = this.createContent(this.getBufferSize());
		byte[] contentD = this.createContent(this.getBufferSize());

		// Write the contents
		this.outputStream.write(contentA);
		this.outputStream.write(contentB);
		this.outputStream.write(contentC);
		this.outputStream.write(contentD);

		// Close the output stream
		this.outputStream.close();

		// Read the results
		this.assertInputStreamRead(contentA);
		CompareBufferProcessor.assertRead(this.input, contentB);
		this.assertInputStreamRead(contentC);
		CompareBufferProcessor.assertRead(this.input, contentD);

		// Ensure end of input stream
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());

		// Close the stream
		this.input.close();
	}

	/**
	 * Ensure able to mix reading bytes and buffers. Buffer read first.
	 */
	public void testCombo_MixReadBufferBytes() throws IOException {

		// Create contents
		byte[] contentA = this.createContent(this.getBufferSize());
		byte[] contentB = this.createContent(this.getBufferSize());
		byte[] contentC = this.createContent(this.getBufferSize());
		byte[] contentD = this.createContent(this.getBufferSize());

		// Write the contents
		this.outputStream.write(contentA);
		this.outputStream.write(contentB);
		this.outputStream.write(contentC);
		this.outputStream.write(contentD);

		// Close the output stream
		this.outputStream.close();

		// Read the results
		CompareBufferProcessor.assertRead(this.input, contentA);
		this.assertInputStreamRead(contentB);
		CompareBufferProcessor.assertRead(this.input, contentC);
		this.assertInputStreamRead(contentD);

		// Ensure end of input stream
		CompareBufferProcessor.assertEndOfStream(this.input);

		// Close the stream
		this.input.close();
	}

	/*
	 * ==================== Helper methods for testing ========================
	 */

	/**
	 * Creates a byte array with populated values.
	 *
	 * @param size
	 *            Size of the content to create.
	 * @return Byte array populated with values.
	 */
	private byte[] createContent(int size) {
		final byte[] content = new byte[size];
		for (int i = 0; i < content.length; i++) {
			content[i] = (byte) (i % 10);
		}
		return content;
	}

	/**
	 * Asserts the byte arrays match.
	 *
	 * @param message
	 *            Message.
	 * @param expected
	 *            Expected byte array.
	 * @param actual
	 *            Actual byte array.
	 */
	private static void assertEquals(byte[] expected, byte[] actual) {
		assertEquals("Incorrect size", expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Incorrect byte at location " + i, expected[i],
					actual[i]);
		}
	}

	/**
	 * Asserts the expected content is read from {@link InputStream}.
	 *
	 * @param expectedContent
	 *            Expected content from the {@link InputStream}.
	 */
	private void assertInputStreamRead(byte[] expectedContent)
			throws IOException {
		byte[] result = new byte[expectedContent.length];
		int readSize = this.inputStream.read(result);
		assertEquals("Incorrect number of bytes read", expectedContent.length,
				readSize);
		assertEquals(expectedContent, result);
	}

	/**
	 * {@link BufferProcessor} that compares {@link ByteBuffer} content to
	 * expected content.
	 */
	private static class CompareBufferProcessor implements BufferProcessor {

		/**
		 * Asserts contents of read.
		 *
		 * @param stream
		 *            {@link InputBufferStream}.
		 * @param expected
		 *            Expected content to read from {@link InputBufferStream}.
		 *            <code>null</code> indicates no data in
		 *            {@link InputBufferStream} to read and not end of stream.
		 */
		public static void assertRead(InputBufferStream stream, byte[] expected) {

			// Do the read
			CompareBufferProcessor processor = new CompareBufferProcessor(
					expected);
			int readSize;
			try {
				readSize = stream.read(processor);
			} catch (Exception ex) {
				fail("Exception not expected: " + ex.getMessage() + " ["
						+ ex.getClass().getName() + "]");
				return; // not reach here as should fail
			}

			// Verify the read
			if (expected == null) {
				// Verify no content read
				assertFalse("Should not be processed",
						processor.isProcessInvoked);
				assertEquals("Should not have read", 0, readSize);
			} else {
				// Verify content read
				assertTrue("Should be processed", processor.isProcessInvoked);
				assertEquals("Incorrect read size", expected.length, readSize);
			}
		}

		/**
		 * Asserts end of stream for the {@link InputBufferStream}.
		 *
		 * @param stream
		 *            {@link InputBufferStream}.
		 */
		public static void assertEndOfStream(InputBufferStream stream) {

			// Do the read
			CompareBufferProcessor processor = new CompareBufferProcessor(null);
			int readSize;
			try {
				readSize = stream.read(processor);
			} catch (Exception ex) {
				fail("Exception not expected: " + ex.getMessage() + " ["
						+ ex.getClass().getName() + "]");
				return; // not reach here as should fail
			}

			// Verify end of stream
			assertFalse("Should not be processed", processor.isProcessInvoked);
			assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
					readSize);
		}

		/**
		 * Expected content.
		 */
		private final byte[] expected;

		/**
		 * Flag indicating if {@link #process(ByteBuffer)} method invoked.
		 */
		public boolean isProcessInvoked = false;

		/**
		 * Initiate.
		 *
		 * @param expected
		 *            Expected content.
		 */
		public CompareBufferProcessor(byte[] expected) {
			this.expected = expected;
		}

		/*
		 * ==================== BufferProcessor ==============================
		 */

		@Override
		public void process(ByteBuffer buffer) throws Exception {

			// Flag invoked
			this.isProcessInvoked = true;

			// Ensure buffer markers are valid
			assertTrue("Buffer not containing expected content", (buffer
					.remaining() >= this.expected.length));

			// Ensure contents are correct
			byte[] actual = new byte[expected.length];
			buffer.get(actual);
			AbstractBufferStreamTest.assertEquals(this.expected, actual);
		}
	}

}