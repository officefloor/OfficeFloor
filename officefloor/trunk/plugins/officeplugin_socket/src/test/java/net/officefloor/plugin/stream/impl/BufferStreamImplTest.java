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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.stream.AbstractBufferStreamTest;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;

/**
 * Tests the {@link BufferStreamImpl}.
 *
 * @author Daniel Sagenschneider
 */
public class BufferStreamImplTest extends AbstractBufferStreamTest implements
		BufferSquirtFactory {

	/**
	 * Buffer size.
	 */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * {@link MockBufferSquirt} instances that must be closed.
	 */
	private final List<MockBufferSquirt> squirts = new LinkedList<MockBufferSquirt>();

	/*
	 * ================= AbstractBufferStreamTest =======================
	 */

	@Override
	protected void tearDown() throws Exception {
		// Ensure squirts are closed
		for (int i = 0; i < this.squirts.size(); i++) {
			assertTrue("Squirt " + i + " must be closed",
					this.squirts.get(i).isClosed);
		}
	}

	@Override
	protected BufferStream createBufferStream() {
		return new BufferStreamImpl(this);
	}

	@Override
	protected int getBufferSize() {
		return BUFFER_SIZE;
	}

	/*
	 * ====================== BufferSquirtFactory ========================
	 */

	@Override
	public BufferSquirt createBufferSquirt() {
		final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		MockBufferSquirt squirt = new MockBufferSquirt(buffer);
		this.squirts.add(squirt);
		return squirt;
	}

	/**
	 * Mock {@link BufferSquirt}.
	 */
	private class MockBufferSquirt implements BufferSquirt {

		/**
		 * {@link ByteBuffer}.
		 */
		private final ByteBuffer buffer;

		/**
		 * Indicates if closed.
		 */
		public boolean isClosed = false;

		/**
		 * Initiate.
		 *
		 * @param buffer
		 *            {@link ByteBuffer}.
		 */
		public MockBufferSquirt(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		/*
		 * ==================== BufferSquirt ============================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return buffer;
		}

		@Override
		public void close() {

			// Ensure not already closed
			assertFalse("Should only be closed once", this.isClosed);

			// Close
			this.isClosed = true;
		}
	}

}