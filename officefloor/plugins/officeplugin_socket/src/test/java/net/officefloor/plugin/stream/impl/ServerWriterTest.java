/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.stream.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.stream.ServerWriter;

/**
 * Tests the {@link ServerWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerWriterTest extends OfficeFrameTestCase {

	/**
	 * {@link OutputStream} to capture written content.
	 */
	private final MockServerOutputStream output = new MockServerOutputStream();

	/**
	 * {@link ServerWriter} to test.
	 */
	private final ServerWriter writer = new ServerWriter(
			this.output.getServerOutputStream(), Charset.defaultCharset(), this);

	/**
	 * Ensure can write text.
	 */
	public void testWriteText() throws IOException {
		this.writer.write("TEST");
		this.writer.flush();
		this.assertWrittenContent("TEST");
	}

	/**
	 * Ensure able to write bytes.
	 */
	public void testWriteBytes() throws IOException {
		this.writer.write("TEST");
		this.writer.write("-BYTES".getBytes());
		this.writer.write("-TEST");
		this.writer.flush();
		this.assertWrittenContent("TEST-BYTES-TEST");
	}

	/**
	 * Ensure able to write cached encoded {@link ByteBuffer}.
	 */
	public void testWriteBuffer() throws IOException {
		this.writer.write("TEST");
		this.writer.write(ByteBuffer.wrap("-BYTES".getBytes()));
		this.writer.write("-TEST");
		this.writer.flush();
		this.assertWrittenContent("TEST-BYTES-TEST");
	}

	/**
	 * Ensure correct written content.
	 * 
	 * @param expectedContent
	 *            Expected content.
	 */
	private void assertWrittenContent(String expectedContent) {
		assertEquals("Incorrect written content", expectedContent, new String(
				this.output.getWrittenBytes()));
	}

}