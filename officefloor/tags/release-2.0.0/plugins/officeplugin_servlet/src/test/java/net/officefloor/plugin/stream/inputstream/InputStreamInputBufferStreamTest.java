/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.stream.inputstream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.impl.OutputBufferStreamImpl;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

/**
 * Tests the {@link InputStreamInputBufferStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputStreamInputBufferStreamTest extends OfficeFrameTestCase {

	/**
	 * Content.
	 */
	private static final String CONTENT = "test";

	/**
	 * Content size.
	 */
	private static final int CONTENT_SIZE = CONTENT.getBytes().length;

	/**
	 * {@link InputBufferStream}.
	 */
	private final InputBufferStream buffer = this
			.createInputBufferStream(CONTENT);

	/**
	 * Ensure provide {@link InputStream}.
	 */
	public void testInputStream() {
		InputStream stream = this.buffer.getInputStream();
		assertContent(CONTENT, stream);
		assertEof(stream);
	}

	/**
	 * Ensure able to browse {@link InputStream}.
	 */
	public void testBrowseStream() {
		// Ensure can browse
		InputStream browse = this.buffer.getBrowseStream();
		assertContent(CONTENT, browse);
		assertEof(browse);

		// Ensure still consume input
		assertContent(CONTENT, this.buffer.getInputStream());
	}

	/**
	 * Ensure coordination of browse.
	 */
	public void testBrowseCoordination() {

		// Read some content
		InputStream stream = this.buffer.getInputStream();
		assertContent("te", stream);

		// Ensure remaining browse content
		InputStream browse = this.buffer.getBrowseStream();
		assertContent("st", browse);
		assertEof(browse);
	}

	/**
	 * Validate available.
	 */
	public void testAvailable() throws Exception {
		assertEquals("Incorrect available", CONTENT.getBytes().length,
				this.buffer.available());
	}

	/**
	 * {@link BufferProcessor}.
	 */
	public void testBufferProcessor() throws Exception {
		final boolean[] isProcessed = new boolean[1];
		isProcessed[0] = false;
		int bytesRead = this.buffer.read(new BufferProcessor() {
			@Override
			public void process(ByteBuffer buffer) throws IOException {
				isProcessed[0] = true;
				assertContent(CONTENT, buffer);
			}
		});
		assertTrue("Must be processed", isProcessed[0]);
		assertEquals("Incorrect number of bytes read", CONTENT_SIZE, bytesRead);
	}

	/**
	 * Ensure read byte array.
	 */
	public void testReadByteArray() throws Exception {
		byte[] actual = new byte[CONTENT_SIZE];
		int bytesRead = this.buffer.read(actual);
		assertEquals("Incorrect bytes read", CONTENT_SIZE, bytesRead);
		assertEquals("Incorrect content", CONTENT, new String(actual));
	}

	/**
	 * Ensure read byte offset.
	 */
	public void testReadByteOffSet() throws Exception {
		byte[] actual = new byte[CONTENT_SIZE];
		int bytesRead = this.buffer.read(actual, 0, actual.length);
		assertEquals("Incorrect bytes read", CONTENT_SIZE, bytesRead);
		assertEquals("Incorrect content", CONTENT, new String(actual));
	}

	/**
	 * {@link GatheringBufferProcessor}.
	 */
	public void testReadGatheringBufferProcessor() throws Exception {
		final boolean[] isProcessed = new boolean[1];
		isProcessed[0] = false;
		int bytesRead = this.buffer.read(CONTENT_SIZE,
				new GatheringBufferProcessor() {
					@Override
					public void process(ByteBuffer[] buffers)
							throws IOException {
						isProcessed[0] = true;
						assertEquals("Incorrect number of buffers", 1,
								buffers.length);
						assertContent(CONTENT, buffers[0]);
					}
				});
		assertTrue("Must be processed", isProcessed[0]);
		assertEquals("Incorrect number of bytes read", CONTENT_SIZE, bytesRead);
	}

	/**
	 * {@link OutputBufferStream}.
	 */
	public void testOutputBufferStream() throws Exception {
		BufferStream bufferStream = new BufferStreamImpl(
				new HeapByteBufferSquirtFactory(CONTENT_SIZE));
		int bytesRead = this.buffer.read(CONTENT_SIZE,
				new OutputBufferStreamImpl(bufferStream));
		byte[] actual = new byte[CONTENT_SIZE];
		bufferStream.read(actual);
		assertEquals("Incorrect number of bytes read", CONTENT_SIZE, bytesRead);
		assertContent(CONTENT, new ByteArrayInputStream(actual));
	}

	/**
	 * Ensure can skip.
	 */
	public void testSkip() throws Exception {
		int skipSize = "te".getBytes().length;
		long bytesSkipped = this.buffer.skip(skipSize);
		assertEquals("Incorrect number of bytes skipped", skipSize,
				bytesSkipped);
		assertContent("st", this.buffer.getInputStream());
	}

	/**
	 * Ensure close.
	 */
	public void testClose() throws Exception {
		final boolean[] isClosed = new boolean[1];
		isClosed[0] = false;
		InputBufferStream buffer = new InputStreamInputBufferStream(
				new InputStream() {
					@Override
					public int read() throws IOException {
						fail("Should not invoke");
						return -1;
					}

					@Override
					public void close() throws IOException {
						isClosed[0] = true;
					}
				});
		buffer.close();
		assertTrue("Ensure closed", isClosed[0]);
	}

	/**
	 * Ensure end of {@link InputStream}.
	 * 
	 * @param stream
	 *            {@link InputStream}.
	 */
	private static void assertEof(InputStream stream) {
		try {
			assertEquals("Should be no content available", 0,
					stream.available());
			assertEquals("Should be EOF", -1, stream.read());
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/**
	 * Assert content.
	 * 
	 * @param expectedContent
	 *            Expected content.
	 * @param buffer
	 *            {@link ByteBuffer} to validate.
	 */
	private static void assertContent(String expectedContent, ByteBuffer buffer) {
		// Ensure appropriate size
		int byteSize = expectedContent.getBytes().length;
		assertEquals("Incorrect buffer size", byteSize, buffer.remaining());

		// Ensure appropriate content
		byte[] actual = new byte[byteSize];
		buffer.get(actual);
		assertEquals("Incorrect buffer content", expectedContent, new String(
				actual));
	}

	/**
	 * Assert content.
	 * 
	 * @param expectedContent
	 *            Expected content.
	 * @param stream
	 *            {@link InputStream} to validate.
	 */
	private static void assertContent(String expectedContent, InputStream stream) {
		try {
			// Ensure available content
			int byteSize = expectedContent.getBytes().length;
			assertTrue("Incorrect available content",
					byteSize <= stream.available());

			// Ensure content is correct
			byte[] actual = new byte[byteSize];
			for (int i = 0; i < byteSize; i++) {
				actual[i] = (byte) stream.read();
			}
			assertEquals("Incorrect content", expectedContent, new String(
					actual));

		} catch (Exception ex) {
			fail(ex);
		}
	}

	/**
	 * Creates the {@link InputBufferStream} with the content.
	 * 
	 * @param content
	 *            Content.
	 * @return {@link InputBufferStream}.
	 */
	private InputBufferStream createInputBufferStream(String content) {
		InputStream inputStream = this.createInputStream(content);
		return new InputStreamInputBufferStream(inputStream);
	}

	/**
	 * Creates the {@link InputStream} with the content.
	 * 
	 * @param content
	 *            Content.
	 * @return {@link InputStream}.
	 */
	private InputStream createInputStream(String content) {
		InputStream inputStream = new ByteArrayInputStream(content.getBytes());
		return inputStream;
	}

}