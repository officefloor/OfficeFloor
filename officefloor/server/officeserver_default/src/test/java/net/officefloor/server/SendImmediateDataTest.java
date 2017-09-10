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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Ensure can send data immmediately.
 * 
 * @author Daniel Sagenschneider
 */
public class SendImmediateDataTest extends AbstractSocketManagerTester {

	@Override
	protected int getBufferSize() {
		return 1024;
	}

	@Override
	protected StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize) {
		return new MockStreamBufferPool(() -> ByteBuffer.allocate(bufferSize));
	}

	@Override
	protected void handleCompletion(StreamBufferPool<ByteBuffer> bufferPool) {
		((MockStreamBufferPool) bufferPool).assertAllBuffersReturned();
	}

	/**
	 * Ensure can send immediate data.
	 */
	public void testImmediateData() throws IOException {
		this.tester = new SocketManagerTester(1);

		// Bind to server socket
		this.tester.bindServerSocket(null, null, (requestHandler) -> (buffer) -> {
			StreamBuffer<ByteBuffer> immediate = this.tester.bufferPool.getPooledStreamBuffer();
			immediate.write((byte) 2);
			requestHandler.sendImmediateData(immediate);
		}, (socketServicer) -> (request, responseWriter) -> {
			fail("Immediate response, so no request to service");
		});

		this.tester.start();

		// Undertake connect and send data
		try (Socket client = this.tester.getClient()) {

			// Send some data (to trigger request)
			OutputStream outputStream = client.getOutputStream();
			outputStream.write(1);
			outputStream.flush();

			// Receive the response
			InputStream inputStream = client.getInputStream();
			assertEquals("Incorrect response", 2, inputStream.read());
		}
	}

}