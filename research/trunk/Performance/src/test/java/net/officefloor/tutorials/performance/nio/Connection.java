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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
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
	 * {@link SelectionKey}.
	 */
	private SelectionKey seletionKey;

	/**
	 * Read {@link ByteBuffer}.
	 */
	private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(2048);

	/**
	 * Indicates if connected.
	 */
	private boolean isConnected = false;

	/**
	 * Number of {@link Request} serviced by this {@link Connection}.
	 */
	private int requestCount = 0;

	/**
	 * Number of times this {@link Connection} reconnected.
	 */
	private int reconnectCount = 0;

	/**
	 * Indicates if this {@link Connection} failed.
	 */
	private boolean isFailed = false;

	/**
	 * <p>
	 * Index of the current {@link Request}.
	 * <p>
	 * Moved to 0 on first {@link Request}.
	 */
	private int currentRequestIndex = 0;

	/**
	 * Obtains the number of times the current {@link Request} has been
	 * repeated.
	 */
	private int currentRepeatCount = 0;

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
	 * Indicates if stopped.
	 */
	private boolean isStopped = false;

	/**
	 * Initiate.
	 * 
	 * @param load
	 *            Containing {@link Load}.
	 * @throws IOException
	 *             If fails to trigger opening a new connection.
	 */
	Connection(Load load) throws IOException {
		this.load = load;

		// Trigger establishing the connection
		this.establishNewConnection();
	}

	/**
	 * Establishes a new connection.
	 * 
	 * @throws IOException
	 *             If fails to trigger opening a new connection.
	 */
	void establishNewConnection() throws IOException {

		// Trigger open of connection
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		Socket socket = channel.socket();
		socket.setReuseAddress(true);
		socket.setSoTimeout(0); // wait forever
		socket.setTcpNoDelay(false);
		channel.connect(new InetSocketAddress(this.load.getRunner()
				.getTargetAddress(), this.load.getRunner().getPort()));

		// Register connection with selector to start requesting
		this.seletionKey = channel.register(this.load.getSelector(),
				SelectionKey.OP_CONNECT, this);
	}

	/**
	 * Stops the {@link Connection}.
	 */
	void stop() {
		this.isStopped = true;
	}

	/**
	 * Finishes the connect.
	 */
	boolean finishConnect() throws IOException {
		this.isConnected = true;
		this.reconnectCount++;
		return ((SocketChannel) this.seletionKey.channel()).finishConnect();
	}

	/**
	 * Sends the request.
	 * 
	 * @return <code>true</code> on full request being written.
	 */
	boolean writeRequest() throws IOException {

		// Determine if stop
		if (this.isStopped) {
			this.seletionKey.channel().close();
			return false; // stopped
		}

		// Obtain the current request
		Request request = this.load.getRequests()[this.currentRequestIndex];
		if (this.currentRepeatCount < request.getRepeatCount()) {
			// Repeat the request
			this.currentRepeatCount++;

		} else {
			// Move to next request
			this.currentRequestIndex = ((this.currentRequestIndex + 1) % this.load
					.getRequests().length);
			this.currentRepeatCount = 0;

			// Determine if finished sequence
			if ((this.load.isDisconnectAfterSequence())
					&& (this.currentRequestIndex == 0)) {

				// Disconnect and reconnect
				this.seletionKey.channel().close();
				this.establishNewConnection();

				// Not written request as disconnecting
				return false;
			}

			// Obtain next request in sequence to process
			request = this.load.getRequests()[this.currentRequestIndex];
		}

		// Write the bytes
		ByteBuffer requestData = request.getData();
		ByteBuffer remaining = requestData.duplicate();
		remaining.position(this.writtenRequestData);
		int numberOfBytesWritten = ((SocketChannel) this.seletionKey.channel())
				.write(remaining);
		this.writtenRequestData += numberOfBytesWritten;

		// Determine if full request written
		if (this.writtenRequestData < requestData.remaining()) {
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

		// Obtain the current request
		Request request = this.load.getRequests()[this.currentRequestIndex];

		// Read from channel
		ByteBuffer buffer = this.readBuffer.duplicate();
		int numberBytesRead = ((SocketChannel) this.seletionKey.channel())
				.read(buffer);
		buffer.flip();
		if (numberBytesRead > 0) {

			try {
				buffer.get(this.responseData, this.availableResponseData,
						numberBytesRead);
			} catch (IndexOutOfBoundsException ex) {
				throw new IOException("Failed reading response", ex);
			}
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

			// Determine if completed response (with content-length or chunked)
			String separater = "\r\n\r\n";
			if (response.contains(separater)
					&& ((!(response.endsWith(separater))))
					|| (response.endsWith("0\r\n\r\n"))) {

				// Determine if failed
				String expectedResponseContent = request
						.getExpectedResponseContent();
				if ((!(response.endsWith(expectedResponseContent)))
						&& (!(response.endsWith(expectedResponseContent
								+ "\r\n0\r\n\r\n")))) {
					throw new IOException("Failed response: " + response + " ["
							+ expectedResponseContent + "]");
				}

				// Read the full response
				this.requestCount++;
				request.requestServiced(this.serviceStart, System.nanoTime());
				this.availableResponseData = 0;
				return true;
			}
		}

		// Response not yet read
		return false;
	}

	/**
	 * Reset counters for next iteration.
	 */
	void reset() {
		this.requestCount = 0;
		this.reconnectCount = 0;
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
	 * Obtains the number of requests serviced.
	 * 
	 * @return Number of requests serviced.
	 */
	int getRequestCount() {
		return this.requestCount;
	}

	/**
	 * Obtains the number of reconnects.
	 * 
	 * @return Number of reconnects.
	 */
	int getReconnectCount() {
		return this.reconnectCount;
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