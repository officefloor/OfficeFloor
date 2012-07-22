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
package net.officefloor.plugin.stream.squirtfactory;

import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * Tests the {@link DirectByteBufferSquirtFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DirectByteBufferSquirtFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link DirectByteBufferSquirtFactory} to test.
	 */
	private final BufferSquirtFactory factory = new DirectByteBufferSquirtFactory(
			1024);

	/**
	 * Ensure correct {@link BufferSquirt}.
	 */
	public void testBufferSquirt() {
		BufferSquirt squirt = this.factory.createBufferSquirt();
		assertSquirt(squirt);
	}

	/**
	 * Ensure can recycle the {@link BufferSquirt}.
	 */
	public void testRecycleBufferSquirt() {

		// Create the squirt
		BufferSquirt squirt = this.factory.createBufferSquirt();

		// Write a value and recycle
		squirt.getBuffer().put((byte) 'c');
		squirt.close();

		// Obtain the squirt
		BufferSquirt recycled = this.factory.createBufferSquirt();
		assertEquals("Must be recycled", (byte) 'c', recycled.getBuffer().get());
	}

	/**
	 * Asserts the {@link BufferSquirt} is correct.
	 * 
	 * @param squirt
	 *            {@link BufferSquirt}.
	 */
	private static void assertSquirt(BufferSquirt squirt) {
		ByteBuffer buffer = squirt.getBuffer();
		assertEquals("Should be at start", 0, buffer.position());
		assertEquals("Incorrect remaining", 1024, buffer.remaining());
		assertEquals("Incorrect limit", 1024, buffer.limit());
		assertEquals("Incorrect capacity", 1024, buffer.capacity());
	}

}