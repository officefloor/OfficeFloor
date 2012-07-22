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
package net.officefloor.tutorials.performance.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Connection.
 * 
 * @author Daniel Sagenschneider
 */
public class Connection {

	/**
	 * HTTP {@link Charset}.
	 */
	public static final Charset CHARSET = Charset.forName("US-ASCII");

	/**
	 * Containing {@link Load}.
	 */
	private final Load load;

	/**
	 * {@link SocketChannel}.
	 */
	private final SocketChannel channel;

	/**
	 * Read {@link ByteBuffer}.
	 */
	private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(2048);

	/**
	 * Indicates if connected.
	 */
	private boolean isConnected = false;

	/**
	 * Indicates if this {@link Connection} failed.
	 */
	private boolean isFailed = false;

	/**
	 * Servicing start time.
	 */
	private long serviceStart = Long.MAX_VALUE;

	/**
	 * Bytes of the request written.
	 */
	private int writtenRequestData = 0;

	/**
	 * Data from response.
	 */
	private final byte[] responseData = new byte[2048];

	/**
	 * Bytes of available response data.
	 */
	private int availableResponseData = 0;

	/**
	 * Initiate.
	 * 
	 * @param load
	 *            Containing {@link Load}.
	 * @param channel
	 *            {@link Connection}.
	 */
	Connection(Load load, SocketChannel channel) {
		this.load = load;
		this.channel = channel;
	}

	/**
	 * Finishes the connect.
	 */
	boolean finishConnect() throws IOException {
		this.isConnected = true;
		return this.channel.finishConnect();
	}

	/**
	 * Sends the request.
	 * 
	 * @return <code>true</code> on full request being written.
	 */
	boolean writeRequest() throws IOException {
		ByteBuffer request = this.load.getRequest();

		// Write the bytes
		ByteBuffer remaining = request.duplicate();
		remaining.position(this.writtenRequestData);
		int numberOfBytesWritten = this.channel.write(this.load.getRequest()
				.duplicate());
		this.writtenRequestData += numberOfBytesWritten;

		// Determine if full request written
		if (this.writtenRequestData < request.remaining()) {
			// Further request data to write
			return false;
		}

		// Request written
		this.serviceStart = System.nanoTime();
		this.writtenRequestData = 0;
		return true;
	}

	/**
	 * Reads the response.
	 * 
	 * @return <code>true</code> on full response being read.
	 * @throws IOException
	 *             If fails to read successful response.
	 */
	boolean readResponse() throws IOException {

		// Read from channel
		ByteBuffer buffer = this.readBuffer.duplicate();
		int numberBytesRead = this.channel.read(buffer);
		buffer.flip();
		if (numberBytesRead > 0) {
			buffer.get(this.responseData, this.availableResponseData,
					numberBytesRead);
			this.availableResponseData += numberBytesRead;

			// Transform into String
			String response = new String(this.responseData, 0,
					this.availableResponseData, CHARSET);

			// Determine if successful response
			String successStatusLine = "HTTP/1.1 200 OK";
			if ((response.length() > successStatusLine.length())
					&& (!(response.startsWith(successStatusLine)))) {
				throw new IOException("Failed response: " + response);
			}

			// Determine if completed response
			String separater = "\r\n\r\n";
			if (response.contains(separater)
					&& (!(response.endsWith(separater)))) {

				// Determine if failed
				String expectedResponseContent = this.load
						.getExpectedResponseContent();
				if (!(response.endsWith(expectedResponseContent))) {
					throw new IOException("Failed response: " + response + " ["
							+ expectedResponseContent + "]");
				}

				// Read the full response
				this.load.requestServiced(this.serviceStart, System.nanoTime());
				this.availableResponseData = 0;
				return true;
			}
		}

		// Response not yet read
		return false;
	}

	/**
	 * Indicates if connected.
	 * 
	 * @return <code>true</code> if connected.
	 */
	boolean isConnected() {
		return this.isConnected;
	}

	/**
	 * Indicates that the connection has failed.
	 */
	void connectionFailed() {
		this.isFailed = true;
	}

	/**
	 * Indicates if failed.
	 * 
	 * @return <code>true</code> if failed.
	 */
	boolean isFailed() {
		return this.isFailed;
	}

}