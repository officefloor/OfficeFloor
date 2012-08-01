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
package net.officefloor.plugin.stream.impl;

import java.io.IOException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.stream.NioInputStream;
import net.officefloor.plugin.stream.NoAvailableInputException;

/**
 * Tests the {@link NioInputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class NioInputStreamTest extends OfficeFrameTestCase {

	/**
	 * {@link NioInputStream}.
	 */
	private final NioInputStreamImpl stream = new NioInputStreamImpl();

	/**
	 * Ensure EOF.
	 */
	public void testEof() throws IOException {
		this.stream.queueData(null, false);
		assertEquals("Should be EOF", -1, this.stream.read());
	}

	/**
	 * Ensure {@link NoAvailableInputException} on reading past available.
	 */
	public void testNoAvailableInput() throws IOException {
		try {
			this.stream.read();
			fail("Should not be successful");
		} catch (NoAvailableInputException ex) {
			// Correct exception
		}
	}

	/**
	 * Ensure can read data.
	 */
	public void testReadData() throws IOException {
		this.stream.queueData(new byte[] { 10 }, false);
		assertEquals("Incorrect read value", 10, this.stream.read());
		assertEquals("Should be EOF", -1, this.stream.read());
	}

	/**
	 * Ensure can read data from multiple backing arrays.
	 */
	public void testReadAcrossMultipleData() throws IOException {
		this.stream.queueData(new byte[] { 11 }, true);
		this.stream.queueData(new byte[] { 22 }, false);
		assertEquals("Incorrect first read", 11, this.stream.read());
		assertEquals("Incorrect second read", 22, this.stream.read());
		assertEquals("Should be EOF", -1, this.stream.read());
	}

}