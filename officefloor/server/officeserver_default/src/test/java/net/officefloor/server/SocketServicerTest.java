/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.stream.BufferPool;

/**
 * Tests the {@link SocketServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketServicerTest extends OfficeFrameTestCase {

	/**
	 * {@link BufferPool}.
	 */
	private final MockBufferPool bufferPool = new MockBufferPool(() -> ByteBuffer.allocateDirect(10));

	/**
	 * Ensure can shutdown the {@link SocketServicer}.
	 */
	public void testShutdown() throws IOException {

		// Create the servicer
		SocketServicer servicer = new SocketServicer(this.bufferPool);
		SocketThread thread = new SocketThread(servicer);

		// Start servicing
		thread.start();

		// Notify to shutdown
		servicer.shutdown();

		// Ensure shut down
		thread.waitForCompletion();
	}

	/**
	 * {@link Socket} {@link Thread} for testing.
	 */
	private class SocketThread extends Thread {

		private final SocketServicer servicer;

		private Object completion = null;

		private SocketThread(SocketServicer servicer) {
			this.servicer = servicer;
		}

		private synchronized void waitForCompletion() {
			long startTime = System.currentTimeMillis();
			while (this.completion == null) {

				// Determine if timed out
				SocketServicerTest.this.timeout(startTime);

				// Not timed out, so wait a little longer
				try {
					this.wait(10);
				} catch (InterruptedException ex) {
					throw SocketServicerTest.fail(ex);
				}
			}
		}

		@Override
		public void run() {
			// Run
			try {
				this.servicer.run();
			} catch (Throwable ex) {
				this.completion = ex;
			} finally {
				// Notify complete
				synchronized (this) {
					if (this.completion == null) {
						this.completion = "COMPLETE";
					}
					this.notify();
				}
			}
		}
	}

}