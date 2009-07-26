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
import java.util.Arrays;

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
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.outputStream.write(VALUE);
		assertEquals("Incorrect available bytes", 1, this.inputStream
				.available());
		int value = this.inputStream.read();
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect result", VALUE, value);
		this.inputStream.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
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
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.outputStream.write(FIRST);
		assertEquals("Incorrect available bytes", 1, this.inputStream
				.available());
		int first = this.inputStream.read();
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect result", FIRST, first);
		final char SECOND = 'b';
		this.outputStream.write(SECOND);
		assertEquals("Incorrect available bytes", 1, this.inputStream
				.available());
		int second = this.inputStream.read();
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect result", SECOND, second);
		this.inputStream.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
	}

	/**
	 * Ensure able to write very large content and read the very large content.
	 */
	public void testStream_OutputInputLargeContent() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 100);

		// Write the content
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.outputStream.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the content
		byte[] result = new byte[content.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) this.inputStream.read();
		}
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Ensure correct result
		assertEquals(content, result);

		// Ensure closed
		this.inputStream.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
	}

	/**
	 * Ensure able to input large content in one read. Content will come from
	 * multiple underlying buffers.
	 */
	public void testStream_InputLargeContent() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 100);

		// Write the content
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.outputStream.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the content in one read
		byte[] result = new byte[content.length];
		int readSize = this.inputStream.read(result);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect number of bytes read", content.length, readSize);

		// Ensure correct result
		assertEquals(content, result);

		// Ensure closed
		this.inputStream.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
	}

	/**
	 * Ensure able to write buffer and read buffer via stream.
	 */
	public void testStream_OutputInputBuffer() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 2);

		// Write the content
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.outputStream.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the content
		byte[] result = new byte[content.length];
		int readSize = this.inputStream.read(result);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect bytes read", content.length, readSize);

		// Ensure correct result
		assertEquals(content, result);

		// Ensure closed
		this.inputStream.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
	}

	/**
	 * Ensure able to write and read buffer with offsets.
	 */
	public void testStream_OffsetBuffer() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 3);

		// Obtain offset and length
		final int offset = content.length / 4;
		final int length = this.getBufferSize() * 2;

		// Write the content
		this.outputStream.write(content, offset, length);
		assertEquals("Incorrect available bytes", length, this.inputStream
				.available());

		// Read the result (via offset)
		final byte[] result = new byte[content.length];
		int readSize = this.inputStream.read(result, offset, length);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect bytes read", length, readSize);

		// Ensure no data before offset and after length
		for (int i = 0; i < offset; i++) {
			assertEquals("Read byte " + i + " should be blank", 0, result[i]);
		}
		for (int i = (offset + length); i < content.length; i++) {
			assertEquals("Read byte " + i + " should be blank", 0, result[i]);
		}

		// Ensure correct result
		byte[] contentCompare = Arrays.copyOfRange(content, offset,
				(offset + length));
		byte[] resultCompare = Arrays.copyOfRange(result, offset,
				(offset + length));
		assertEquals(contentCompare, resultCompare);

		// Close stream
		this.inputStream.close();
	}

	/**
	 * Ensure skip of empty stream works as likely override default skip
	 * routine.
	 */
	public void testStream_SkipEmptyStream() throws IOException {
		InputStream inputStream = this.input.getInputStream();
		assertEquals("Should not skip if negative", 0, inputStream.skip(-1));
		assertEquals("No effect if skip zero", 0, inputStream.skip(0));
		assertEquals("Should not skip if empty", 0, inputStream.skip(10));
	}

	/**
	 * Ensure able to skip single byte as likely override default skip routine.
	 */
	public void testStream_SkipSingleByte() throws IOException {
		// Add a byte
		this.outputStream.write('a');

		// Ensure can skip
		InputStream inputStream = this.input.getInputStream();
		assertEquals("Should not skip if negative", 0, inputStream.skip(-1));
		assertEquals("No effect if skip zero", 0, inputStream.skip(0));
		assertEquals("Should only skip available", 1, inputStream.skip(10));
		assertEquals("Should not skip as no available", 0, inputStream.skip(10));

		// Close output to indicate no further data
		this.output.close();

		// Should now be end of stream
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				inputStream.skip(10));
	}

	/**
	 * Ensure able to skip over multiple buffers as likely override default skip
	 * routine.
	 */
	public void testStream_SkipOverMultipleBuffers() throws IOException {

		// Write content
		byte[] content = this.createContent(this.getBufferSize() * 3);
		this.output.write(content);

		// Input the content
		InputStream inputStream = this.input.getInputStream();
		assertEquals("Should not skip if negative", 0, inputStream.skip(-1));
		assertEquals("No effect if skip zero", 0, inputStream.skip(0));

		// Skip into first buffer and validate position
		int position = this.getBufferSize() / 3;
		assertEquals("Should skip the bytes", position, inputStream
				.skip(position));
		byte expectedByte = content[position];
		byte actualByte = (byte) inputStream.read();
		assertEquals("Incorrect byte after skip", expectedByte, actualByte);
		position++; // at next position after read

		// Skip into second buffer and validate position
		position += this.getBufferSize();
		assertEquals("Should skip to next buffer", this.getBufferSize(),
				inputStream.skip(this.getBufferSize()));
		expectedByte = content[position];
		actualByte = (byte) inputStream.read();
		assertEquals("Incorrect byte after skip to next buffer", expectedByte,
				actualByte);
		position++; // at next position after read

		// Verify not skipping while in middle of available data
		assertEquals("Should not skip if negative", 0, inputStream.skip(-1));
		assertEquals("No effect if skip zero", 0, inputStream.skip(0));
		expectedByte = content[position];
		actualByte = (byte) inputStream.read();
		assertEquals("Incorrect byte after no effect operations", expectedByte,
				actualByte);
		position++; // at next position after read

		// Skip past end and ensure end of stream
		int remainingPositions = content.length - position;
		assertEquals("Should only skip remaining positions",
				remainingPositions, inputStream.skip(content.length * 10));
		assertEquals("Further skips should not move", 0, inputStream.skip(1));

		// Close output to indicate no further data
		this.output.close();

		// Should now be end of stream
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				inputStream.skip(10));
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
		assertEquals("Incorrect available bytes", 1, this.inputStream
				.available());
		assertEquals("Incorrect read byte", 'a', this.inputStream.read());
		assertEquals("Should now be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// Verify that from now on indicates that closed
		for (int i = 0; i < 1000; i++) {
			assertEquals("Should always be end of stream",
					BufferStream.END_OF_STREAM, this.inputStream.read());
			assertEquals("Incorrect available bytes",
					BufferStream.END_OF_STREAM, this.inputStream.available());
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
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
		assertEquals("Should now be end of stream", BufferStream.END_OF_STREAM,
				this.inputStream.read());
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
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
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.output.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the bytes
		byte[] result = new byte[content.length];
		int readSize = this.input.read(result);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		assertEquals("Incorrect number of bytes read", content.length, readSize);

		// Ensure read results are correct
		assertEquals(content, result);

		// Close the stream
		this.input.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
	}

	/**
	 * Ensure able to output and input large byte content.
	 */
	public void testBytes_OutputInputLargeContent() throws IOException {

		// Create the content
		byte[] content = this.createContent(this.getBufferSize() * 100);

		// Write the bytes
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.output.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the bytes
		byte[] result = new byte[content.length];
		int readSize = this.input.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Ensure read results are correct
		assertEquals(content, result);

		// Close the stream
		this.input.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
	}

	/**
	 * Ensure able to write and read buffer with offsets.
	 */
	public void testBytes_OffsetBytes() throws IOException {

		// Create the content to write
		final byte[] content = this.createContent(this.getBufferSize() * 3);

		// Obtain offset and length
		final int offset = content.length / 4;
		final int length = this.getBufferSize() * 2;

		// Write the content
		this.output.write(content, offset, length);
		assertEquals("Incorrect available bytes", length, this.input
				.available());

		// Read the result (via offset)
		final byte[] result = new byte[content.length];
		int readSize = this.input.read(result, offset, length);
		assertEquals("Incorrect available bytes", 0, this.input.available());
		assertEquals("Incorrect bytes read", length, readSize);

		// Ensure no data before offset and after length
		for (int i = 0; i < offset; i++) {
			assertEquals("Read byte " + i + " should be blank", 0, result[i]);
		}
		for (int i = (offset + length); i < content.length; i++) {
			assertEquals("Read byte " + i + " should be blank", 0, result[i]);
		}

		// Ensure correct result
		byte[] contentCompare = Arrays.copyOfRange(content, offset,
				(offset + length));
		byte[] resultCompare = Arrays.copyOfRange(result, offset,
				(offset + length));
		assertEquals(contentCompare, resultCompare);

		// Close stream
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
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.output.append(contentBuffer);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the bytes
		byte[] result = new byte[content.length];
		int readSize = this.input.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

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
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the byte buffer
		CompareBufferProcessor.assertRead(this.input, content);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Ensure data read and no further data available
		CompareBufferProcessor.assertRead(this.input, null);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Ensure verify end of stream
		this.output.close();
		CompareBufferProcessor.assertEndOfStream(this.input);
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

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
		assertEquals("Incorrect available bytes",
				(contentA.length + contentB.length), this.inputStream
						.available());

		// Read the buffer contents in partial chunks
		CompareBufferProcessor.assertRead(this.input, contentA);
		assertEquals("Incorrect available bytes", contentB.length,
				this.inputStream.available());

		// Ensure on output close that can continue reading remaining
		this.output.close();
		CompareBufferProcessor.assertRead(this.input, contentB);
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// Ensure now end of stream
		CompareBufferProcessor.assertEndOfStream(this.input);
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// All squirts should be closed
	}

	/**
	 * Ensure that appended {@link ByteBuffer} is same read {@link ByteBuffer}.
	 */
	public void testBuffer_SameBuffer() throws IOException {

		// Create buffer to append
		byte[] content = this.createContent(10);
		ByteBuffer contentBuffer = ByteBuffer.wrap(content);

		// Append the buffer
		this.output.append(contentBuffer);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the buffer
		final ByteBuffer[] processBuffer = new ByteBuffer[1];
		this.input.read(new BufferProcessor() {
			@Override
			public void process(ByteBuffer buffer) {
				processBuffer[0] = buffer;
			}
		});
		assertEquals("Incorrect available bytes", 10, this.inputStream
				.available());

		// Validate buffer (not same buffer as duplicate)
		byte[] result = new byte[content.length];
		processBuffer[0].get(result);
		assertEquals(content, result);
	}

	/**
	 * Ensure can populate {@link ByteBuffer}.
	 */
	public void testBuffer_PopulateBuffer() throws IOException {

		// Populate the content
		byte[] content = this.createContent(this.getBufferSize());
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());
		this.output.write(new LoadBufferPopulator(content));
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read the content
		this.assertInputStreamRead(content);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Close stream
		this.input.close();
	}

	/**
	 * Ensure can partially populate the {@link ByteBuffer}.
	 */
	public void testBuffer_PartialPopulateBuffer() throws IOException {

		// Create the partial content
		byte[] contentA = this.createContent(this.getBufferSize() / 3);
		byte[] contentB = this.createContent(this.getBufferSize() / 4);

		// Partially populate the content
		this.output.write(new LoadBufferPopulator(contentA));
		assertEquals("Incorrect available bytes", contentA.length,
				this.inputStream.available());
		this.output.write(new LoadBufferPopulator(contentB));
		assertEquals("Incorrect available bytes",
				(contentA.length + contentB.length), this.inputStream
						.available());

		// Close the output
		this.output.close();

		// Read the content
		this.assertInputStreamRead(contentA);
		assertEquals("Incorrect available bytes", contentB.length,
				this.inputStream.available());
		this.assertInputStreamRead(contentB);
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// Close stream
		this.input.close();
	}

	/*
	 * ==================== Browse tests ================================
	 */

	/**
	 * Ensure end of stream if no data on browse.
	 */
	public void testBrowse_EmptyStream() throws IOException {
		InputStream browseStream = this.input.getBrowseStream();
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				browseStream.read());
	}

	/**
	 * Ensure able to browse a single byte.
	 */
	public void testBrowse_SingleByte() throws IOException {

		// Add a byte
		this.outputStream.write('a');

		// Ensure can browse the single byte
		InputStream browseStream = this.input.getBrowseStream();
		assertEquals("Incorrect browsed byte", 'a', browseStream.read());
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				browseStream.read());

		// Ensure content still available to read
		this.assertInputStreamRead(new byte[] { 'a' });

		// Close stream
		this.input.close();
	}

	/**
	 * Ensure able to browse content should {@link BufferStream} have multiple
	 * underlying {@link BufferSquirt} instances.
	 */
	public void testBrowse_MultipleBuffers() throws IOException {

		// Write content
		byte[] content = this.createContent(this.getBufferSize() * 3);
		this.output.write(content);

		// Browse the content
		InputStream browseStream = this.input.getBrowseStream();
		byte[] result = new byte[content.length];
		int readSize = browseStream.read(result);
		assertEquals("Incorrect read size", content.length, readSize);
		assertEquals(content, result);
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				browseStream.read());

		// Ensure content still available to read
		this.assertInputStreamRead(content);

		// Close stream
		this.input.close();
	}

	/**
	 * Ensure skip of empty stream works as likely override default skip
	 * routine.
	 */
	public void testBrowse_SkipEmptyStream() throws IOException {
		InputStream browseStream = this.input.getBrowseStream();
		assertEquals("Should not skip if negative", 0, browseStream.skip(-1));
		assertEquals("No effect if skip zero", 0, browseStream.skip(0));
		assertEquals("Should not skip if empty", 0, browseStream.skip(10));
	}

	/**
	 * Ensure able to skip single byte as likely override default skip routine.
	 */
	public void testBrowse_SkipSingleByte() throws IOException {
		// Add a byte
		this.outputStream.write('a');

		// Ensure can skip
		InputStream browseStream = this.input.getBrowseStream();
		assertEquals("Should not skip if negative", 0, browseStream.skip(-1));
		assertEquals("No effect if skip zero", 0, browseStream.skip(0));
		assertEquals("Should only skip available", 1, browseStream.skip(10));
		assertEquals("Should be end of stream", BufferStream.END_OF_STREAM,
				browseStream.read());

		// Ensure content still available to read
		this.assertInputStreamRead(new byte[] { 'a' });

		// Close stream
		this.input.close();
	}

	/**
	 * Ensure able to skip over multiple buffers as likely override default skip
	 * routine.
	 */
	public void testBrowse_SkipOverMultipleBuffers() throws IOException {

		// Write content
		byte[] content = this.createContent(this.getBufferSize() * 3);
		this.output.write(content);

		// Browse the content
		InputStream browseStream = this.input.getBrowseStream();
		assertEquals("Should not skip if negative", 0, browseStream.skip(-1));
		assertEquals("No effect if skip zero", 0, browseStream.skip(0));

		// Skip into first buffer and validate position
		int position = this.getBufferSize() / 3;
		assertEquals("Should skip the bytes", position, browseStream
				.skip(position));
		byte expectedByte = content[position];
		byte actualByte = (byte) browseStream.read();
		assertEquals("Incorrect byte after skip", expectedByte, actualByte);
		position++; // at next position after read

		// Skip into second buffer and validate position
		position += this.getBufferSize();
		assertEquals("Should skip to next buffer", this.getBufferSize(),
				browseStream.skip(this.getBufferSize()));
		expectedByte = content[position];
		actualByte = (byte) browseStream.read();
		assertEquals("Incorrect byte after skip to next buffer", expectedByte,
				actualByte);
		position++; // at next position after read

		// Verify not skipping while in middle of available data
		assertEquals("Should not skip if negative", 0, browseStream.skip(-1));
		assertEquals("No effect if skip zero", 0, browseStream.skip(0));
		expectedByte = content[position];
		actualByte = (byte) browseStream.read();
		assertEquals("Incorrect byte after no effect operations", expectedByte,
				actualByte);
		position++; // at next position after read

		// Skip past end and ensure end of stream
		int remainingPositions = content.length - position;
		assertEquals("Should only skip remaining positions",
				remainingPositions, browseStream.skip(content.length * 10));
		assertEquals("Further skips should not move", 0, browseStream.skip(1));
		assertEquals("Should be end of browse stream",
				BufferStream.END_OF_STREAM, browseStream.read());

		// Ensure content still available to read
		this.assertInputStreamRead(content);

		// Close stream
		this.input.close();
	}

	/*
	 * ==================== Transfer Stream tests ==============================
	 */

	/**
	 * Ensure able to stream content from one {@link BufferStream} to another
	 * {@link BufferStream}.
	 */
	public void testTransfer_FullBuffer() throws IOException {

		// Populate the buffer stream
		byte[] content = this.createContent(this.getBufferSize());
		this.outputStream.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Create another buffer stream
		BufferStream joinStream = this.createBufferStream();
		OutputBufferStream joinOutput = joinStream.getOutputBufferStream();

		// Read contents from buffer stream into other buffer stream
		int readSize = this.input.read(content.length, joinOutput);
		assertEquals("Incorrect transfer size of bytes", content.length,
				readSize);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Validate no further data in original buffer stream
		this.outputStream.close();
		assertEquals("No further data for original",
				BufferStream.END_OF_STREAM, this.inputStream.read());
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// Validate data transferred to other buffer stream
		InputBufferStream joinInput = joinStream.getInputBufferStream();
		assertEquals("Incorrect available bytes", content.length, joinInput
				.available());
		byte[] result = new byte[content.length];
		readSize = joinInput.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);
		assertEquals("Incorrect available bytes", 0, joinInput.available());
		assertEquals(content, result);

		// Close stream
		joinInput.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				joinInput.available());
	}

	/**
	 * Ensure able to transfer partial content of a buffer.
	 */
	public void testTransfer_PartialBuffer() throws IOException {

		// Populate the buffer stream
		byte[] content = this.createContent(this.getBufferSize());
		this.outputStream.write(content);
		this.outputStream.close();
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Create another buffer stream
		BufferStream joinStream = this.createBufferStream();
		OutputBufferStream joinOutput = joinStream.getOutputBufferStream();

		// Obtain the bytes to transfer
		int transferSize = this.getBufferSize() / 3;
		int remainingSize = this.getBufferSize() - transferSize;

		// Read contents from buffer stream into other buffer stream
		int readSize = this.input.read(transferSize, joinOutput);
		assertEquals("Incorrect transfer size of bytes", transferSize, readSize);
		assertEquals("Incorrect remaining available bytes", remainingSize,
				this.inputStream.available());

		// Validate the remaining data in original stream
		byte[] originalResult = new byte[remainingSize];
		readSize = this.input.read(originalResult);
		byte[] originalContent = Arrays.copyOfRange(content, transferSize,
				content.length);
		assertEquals(originalContent, originalResult);
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());
		assertEquals("No further data for original",
				BufferStream.END_OF_STREAM, this.inputStream.read());

		// Validate data transferred to other buffer stream
		InputBufferStream joinInput = joinStream.getInputBufferStream();
		assertEquals("Incorrect available bytes", transferSize, joinInput
				.available());
		byte[] transferredResult = new byte[transferSize];
		readSize = joinInput.read(transferredResult);
		assertEquals("Incorrect number of bytes read", transferSize, readSize);
		assertEquals("Incorrect available bytes", 0, joinInput.available());
		byte[] transferredContent = Arrays
				.copyOfRange(content, 0, transferSize);
		assertEquals(transferredContent, transferredResult);

		// Close stream
		joinInput.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				joinInput.available());
	}

	/**
	 * Enusre able to transfer multiple buffers.
	 */
	public void testTransfer_MultipleBuffers() throws IOException {

		// Populate the buffer stream
		byte[] content = this.createContent(this.getBufferSize() * 3);
		this.outputStream.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Create another buffer stream
		BufferStream joinStream = this.createBufferStream();
		OutputBufferStream joinOutput = joinStream.getOutputBufferStream();

		// Read contents from buffer stream into other buffer stream
		int readSize = this.input.read(content.length, joinOutput);
		assertEquals("Incorrect transfer size of bytes", content.length,
				readSize);
		assertEquals("Incorrect available bytes", 0, this.inputStream
				.available());

		// Validate no further data in original buffer stream
		this.outputStream.close();
		assertEquals("No further data for original",
				BufferStream.END_OF_STREAM, this.inputStream.read());
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// Validate data transferred to other buffer stream
		InputBufferStream joinInput = joinStream.getInputBufferStream();
		assertEquals("Incorrect available bytes", content.length, joinInput
				.available());
		byte[] result = new byte[content.length];
		readSize = joinInput.read(result);
		assertEquals("Incorrect number of bytes read", content.length, readSize);
		assertEquals("Incorrect available bytes", 0, joinInput.available());
		assertEquals(content, result);

		// Close stream
		joinInput.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				joinInput.available());
	}

	/**
	 * Ensure able to transfer content when positioned not at a start of a
	 * buffer.
	 */
	public void testTransfer_OffsetIntoBuffer() throws IOException {

		// Populate the buffer stream
		byte[] content = this.createContent(this.getBufferSize());
		this.outputStream.write(content);
		assertEquals("Incorrect available bytes", content.length,
				this.inputStream.available());

		// Read byte from stream
		assertEquals("Incorrect first byte", content[0], this.inputStream
				.read());
		assertEquals("Incorrect available bytes", (content.length - 1),
				this.inputStream.available());

		// Create another buffer stream
		BufferStream joinStream = this.createBufferStream();
		OutputBufferStream joinOutput = joinStream.getOutputBufferStream();

		// Obtain transfer size
		int transferSize = this.getBufferSize() / 3;
		int remainingSize = content.length - 1 - transferSize; // -1 for read

		// Read contents from buffer stream into other buffer stream
		int readSize = this.input.read(transferSize, joinOutput);
		assertEquals("Incorrect transfer size of bytes", transferSize, readSize);
		assertEquals("Incorrect available bytes", remainingSize,
				this.inputStream.available());

		// Validate the remaining data in original buffer
		this.outputStream.close();
		byte[] originalResult = new byte[remainingSize];
		assertEquals("Incorrect remaining read size", remainingSize,
				this.inputStream.read(originalResult));
		byte[] orginalContent = Arrays.copyOfRange(content, (1 + transferSize),
				content.length); // + 1 for read
		assertEquals(orginalContent, originalResult);
		assertEquals("No further data for original",
				BufferStream.END_OF_STREAM, this.inputStream.read());
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				this.inputStream.available());

		// Validate data transferred to other buffer stream
		InputBufferStream joinInput = joinStream.getInputBufferStream();
		assertEquals("Incorrect available bytes", transferSize, joinInput
				.available());
		byte[] transferredResult = new byte[transferSize];
		readSize = joinInput.read(transferredResult);
		assertEquals("Incorrect number of bytes read", transferSize, readSize);
		assertEquals("Incorrect available bytes", 0, joinInput.available());
		byte[] transferredContent = Arrays.copyOfRange(content, 1,
				(1 + transferSize)); // 1 for read
		assertEquals(transferredContent, transferredResult);

		// Close stream
		joinInput.close();
		assertEquals("Incorrect available bytes", BufferStream.END_OF_STREAM,
				joinInput.available());
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
		public void process(ByteBuffer buffer) {

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

	/**
	 * {@link BufferPopulator} to load bytes.
	 */
	private static class LoadBufferPopulator implements BufferPopulator {

		/**
		 * Content to load to the {@link ByteBuffer}.
		 */
		private final byte[] content;

		/**
		 * Initiate.
		 *
		 * @param content
		 *            Content to load to the {@link ByteBuffer}.
		 */
		public LoadBufferPopulator(byte[] content) {
			this.content = content;
		}

		/*
		 * =================== BufferPopulator ===============================
		 */

		@Override
		public void populate(ByteBuffer buffer) throws IOException {
			buffer.put(this.content);
		}
	}

}