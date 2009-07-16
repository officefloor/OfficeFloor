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
	 * {@link InputBufferStream}.
	 */
	private final InputBufferStream input = this.bufferStream
			.getInputBufferStream();

	/**
	 * {@link OutputBufferStream}.
	 */
	private final OutputBufferStream output = this.bufferStream
			.getOutputBufferStream();

	/**
	 * Ensure able to output and input a byte via streams.
	 */
	public void testOutputInputByteViaSteams() throws IOException {
		OutputStream outputStream = this.output.getOutputStream();
		InputStream inputStream = this.input.getInputStream();
		final char VALUE = 'a';
		outputStream.write(VALUE);
		int value = inputStream.read();
		assertEquals("Incorrect result", VALUE, value);
	}

	/**
	 * Ensure issue if attempting to read when no data is available.
	 */
	public void testNoDataOnBlockingRead() throws IOException {
		InputStream inputStream = this.input.getInputStream();
		try {
			inputStream.read();
			fail("Should not read data");
		} catch (NoBufferStreamContentException ex) {
			// Correct exception
		}
	}

	/**
	 * Ensure able to output, input, output and input a byte via streams.
	 */
	public void testOutputInputOutputInputByteViaSteams() throws IOException {
		OutputStream outputStream = this.output.getOutputStream();
		InputStream inputStream = this.input.getInputStream();
		final char FIRST = 'a';
		outputStream.write(FIRST);
		int first = inputStream.read();
		assertEquals("Incorrect result", FIRST, first);
		final char SECOND = 'b';
		outputStream.write(SECOND);
		int second = inputStream.read();
		assertEquals("Incorrect result", SECOND, second);
	}

	/**
	 * Ensure able to write very large content and read the very large content.
	 */
	public void testOutputInputLargeContent() throws IOException {
		// Create the content to write
		final byte[] content = new byte[1024 * 1024];
		for (int i = 0; i < content.length; i++) {
			content[i] = (byte) (i % 10);
		}

		// Write the content
		this.output.getOutputStream().write(content);

		// Read the content
		InputStream inputStream = this.input.getInputStream();
		byte[] result = new byte[content.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) inputStream.read();
		}

		// Ensure correct result
		for (int i = 0; i < content.length; i++) {
			assertEquals("Incorrect byte at location " + i, content[i],
					result[i]);
		}
	}

}