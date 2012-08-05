/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.stream.servlet;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ServletServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerOutputStreamTest extends OfficeFrameTestCase {

	/**
	 * Content.
	 */
	private final String CONTENT = "test";

	/**
	 * Content bytes.
	 */
	private final byte[] CONTENT_BYTES = CONTENT.getBytes();

	/**
	 * Content received.
	 */
	private final ByteArrayOutputStream content = new ByteArrayOutputStream();

	/**
	 * TODO implement tests (to write to backing {@link OutputStream}).
	 */
	public void test_TODO_implement() {
		fail("TODO implement tests for "
				+ ServletServerOutputStream.class.getSimpleName());
	}

//	/**
//	 * {@link OutputBufferStream} to test.
//	 */
//	private final OutputBufferStream buffer = new ServletServerOutputStream(
//			this.content);
//
//	/**
//	 * {@link OutputStream}.
//	 */
//	public void testOutputStream() throws Exception {
//		this.buffer.getOutputStream().write(CONTENT_BYTES);
//		this.assertContent(CONTENT);
//	}
//
//	/**
//	 * {@link BufferSquirt}.
//	 */
//	public void testByteBuffer() throws Exception {
//		ByteBuffer data = ByteBuffer.wrap(CONTENT_BYTES);
//		this.buffer.append(data);
//		this.assertContent(CONTENT);
//	}
//
//	/**
//	 * {@link BufferSquirt}.
//	 */
//	public void testBufferSquirt() throws Exception {
//		ByteBuffer data = ByteBuffer.wrap(CONTENT_BYTES);
//		BufferSquirt squirt = new BufferSquirtImpl(data);
//		this.buffer.append(squirt);
//		this.assertContent(CONTENT);
//	}
//
//	/**
//	 * Ensure can write bytes array.
//	 */
//	public void testWriteBytesArray() throws Exception {
//		this.buffer.write(CONTENT_BYTES);
//		this.assertContent(CONTENT);
//	}
//
//	/**
//	 * Ensure can write bytes offset.
//	 */
//	public void testWriteBytesOffset() throws Exception {
//		this.buffer.write(CONTENT_BYTES, 0, CONTENT_BYTES.length);
//		this.assertContent(CONTENT);
//	}
//
//	/**
//	 * {@link BufferPopulator}.
//	 */
//	public void testBufferPopulator() throws Exception {
//		this.buffer.write(new BufferPopulator() {
//			@Override
//			public void populate(ByteBuffer buffer) throws IOException {
//				buffer.put(CONTENT_BYTES);
//			}
//		});
//		this.assertContent(CONTENT);
//	}
//
//	/**
//	 * Ensure can close.
//	 */
//	public void testClose() throws Exception {
//		final boolean[] isClosed = new boolean[1];
//		isClosed[0] = false;
//		OutputStream output = new OutputStream() {
//			@Override
//			public void write(int b) throws IOException {
//				fail("Should not be invoked");
//			}
//
//			@Override
//			public void close() throws IOException {
//				isClosed[0] = true;
//			}
//		};
//
//		// Ensure close
//		OutputBufferStream buffer = new ServletServerOutputStream(output);
//		buffer.close();
//		assertTrue("Should be closed", isClosed[0]);
//	}
//
//	/**
//	 * Asserts the content.
//	 * 
//	 * @param expectedContent
//	 *            Expected content.
//	 */
//	private void assertContent(String expectedContent) {
//		String actualContent = new String(this.content.toByteArray());
//		assertEquals("Incorrect content", expectedContent, actualContent);
//	}

}