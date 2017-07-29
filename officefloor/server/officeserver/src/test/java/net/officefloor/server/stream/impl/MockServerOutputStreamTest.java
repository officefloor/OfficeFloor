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
package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.impl.MockServerOutputStream;

/**
 * Tests the {@link MockServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOutputStreamTest extends OfficeFrameTestCase {

	/**
	 * {@link MockServerOutputStream} to test.
	 */
	private final MockServerOutputStream mock = new MockServerOutputStream();

	/**
	 * {@link ServerOutputStream}.
	 */
	private final ServerOutputStream output = this.mock.getServerOutputStream();

	/**
	 * {@link ServerWriter}.
	 */
	private final ServerWriter writer = this.mock.getServerWriter();

	/**
	 * Ensure able to write the data and obtain.
	 */
	public void testWriteData() throws IOException {

		// Write the data
		this.output.write("TEST".getBytes());
		this.output.flush();

		// Validate output
		byte[] writtenBytes = this.mock.getWrittenBytes();
		assertEquals("Incorrect written bytes", "TEST",
				new String(writtenBytes));
	}

	/**
	 * Ensure able to write a {@link ByteBuffer} and obtain.
	 */
	public void testWriteBuffer() throws IOException {

		// Create the buffer
		ByteBuffer buffer = ByteBuffer.wrap("TEST".getBytes());

		// Write the buffer
		this.output.write(buffer);
		this.output.close();

		// Validate output
		byte[] writtenBytes = this.mock.getWrittenBytes();
		assertEquals("Incorrect written bytes", "TEST",
				new String(writtenBytes));
	}

	/**
	 * Ensure able to write text and obtain.
	 */
	public void testWriteText() throws IOException {

		// Write the text
		this.writer.write("TEST");

		// Validate otuput
		this.mock.flush();
		assertEquals("Incorrect written text", "TEST",
				new String(this.mock.getWrittenBytes()));
	}

}