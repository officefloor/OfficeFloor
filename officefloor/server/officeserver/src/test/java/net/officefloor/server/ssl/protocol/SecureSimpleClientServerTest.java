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
package net.officefloor.server.ssl.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.impl.SimpleClientServerTest;
import net.officefloor.server.protocol.CommunicationProtocolSource;
import net.officefloor.server.ssl.OfficeFloorDefaultSslEngineSource;
import net.officefloor.server.ssl.protocol.SslCommunicationProtocol;

/**
 * Secure {@link SimpleClientServerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureSimpleClientServerTest extends SimpleClientServerTest {

	/**
	 * Client {@link SSLEngine}.
	 */
	private SSLEngine clientEngine;

	@Override
	protected void setUp() throws Exception {

		// Establish the connection
		super.setUp();

		// Create the client side of connection
		SSLContext context = OfficeFloorDefaultSslEngineSource.createClientSslContext(null);
		this.clientEngine = context.createSSLEngine("server", 443);
		this.clientEngine.setUseClientMode(true);

		// Undertake the handshake
		this.clientEngine.beginHandshake();
		this.doHandshake();
	}

	/**
	 * Undertakes the handshake.
	 */
	private void doHandshake() throws IOException {

		// Indicate progress
		System.out.println("HANDSHAKE: start");

		HANDSHAKE: for (;;) {
			HandshakeStatus status = this.clientEngine.getHandshakeStatus();
			switch (status) {
			case NEED_WRAP:
				this.doWrap(null, true);
				this.runServerSelect(); // service sent data
				break;

			case NEED_UNWRAP:
				this.doUnwrap(true);
				break;

			case NOT_HANDSHAKING:
				break HANDSHAKE;

			default:
				fail("Invalid status " + status);
			}
		}

		// Indicate progress
		System.out.println("HANDSHAKE: stop");
	}

	@Override
	protected CommunicationProtocolSource getCommunicationProtocolSource() {
		return new SslCommunicationProtocol(this);
	}

	@Override
	protected Properties getCommunicationProtocolProperties() {
		Properties properties = new Properties();
		properties.setProperty(SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
				OfficeFloorDefaultSslEngineSource.class.getName());
		return properties;
	}

	@Override
	protected void handleInvokeProcess(Object parameter, ManagedObject managedObject, FlowCallback callback) {

		// Invoke the runnable for the server
		Runnable runnable = (Runnable) parameter;
		runnable.run();
	}

	@Override
	protected void writeDataFromClientToServer(byte[] data) {
		try {

			// Client handle SSL
			this.doWrap(data, false);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	@Override
	protected void assertClientReceivedData(byte[] expectedBytes) {
		try {

			// Determine if expecting close
			if (expectedBytes == null) {
				this.doHandshake();
			}

			// Undertake the close check
			super.assertClientReceivedData(expectedBytes);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	@Override
	protected int writeClientReceivedData(OutputStream outputStream) {
		try {

			// Client handle SSL
			byte[] bytes = this.doUnwrap(false);
			if (bytes != null) {
				outputStream.write(bytes);
			}

			// Return the bytes read
			return (bytes == null ? -1 : bytes.length);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	@Override
	protected void closeClient() throws IOException {

		// Close and undertake close handshake
		this.clientEngine.closeOutbound();
		this.doHandshake();
	}

	/**
	 * Does the wrap and send the data to the Server.
	 * 
	 * @param isHandshake
	 *            Indicates if undertaking handshake.
	 * @param data
	 *            Data to be sent.
	 */
	private void doWrap(byte[] data, boolean isHandshake) throws IOException {

		boolean isSentData = false;
		for (;;) {

			// Handle handshake
			boolean isWrap = false;
			HandshakeStatus handshakeStatus = this.clientEngine.getHandshakeStatus();
			switch (handshakeStatus) {

			case NEED_UNWRAP:
				assertTrue("Should have sent some data", isSentData);
				return; // await response

			case NOT_HANDSHAKING:
				if (isHandshake) {
					return; // handshake established
				}
			case NEED_WRAP:
				isWrap = true;
				break;

			case NEED_TASK:

				// Indicate progress
				System.out.println("CLIENT: task");

				this.clientEngine.getDelegatedTask().run();
				break; // allow to undertake another tasks

			default:
				fail("Handshake status " + handshakeStatus);
			}

			// Determine if wrap and send data
			if (isWrap) {

				// Indicate progress
				System.out.println("CLIENT: wrap");

				// Create the plain data
				ByteBuffer plainBuffer = ByteBuffer.wrap(data == null ? new byte[0] : data);

				// Create buffer for cipher data
				SSLSession session = this.clientEngine.getSession();
				int packetBufferSize = session.getPacketBufferSize();
				ByteBuffer cipherBuffer = ByteBuffer.allocate(packetBufferSize);

				// Wrap up the data
				SSLEngineResult result = this.clientEngine.wrap(plainBuffer, cipherBuffer);
				Status status = result.getStatus();
				switch (status) {
				case CLOSED:
					assertNull("Should not be expecting data on close", data);
				case OK:
					break;
				default:
					fail("Should have wrapped data: " + status);
				}
				assertEquals("Not all bytes consumed", (data == null ? 0 : data.length), result.bytesConsumed());

				// Write the data from client to server
				cipherBuffer.flip();
				byte[] cipherData = new byte[cipherBuffer.remaining()];
				cipherBuffer.get(cipherData);
				assertTrue("Must have data to send to server", (cipherData.length > 0));
				super.writeDataFromClientToServer(cipherData);

				// Flag that data sent to the server
				isSentData = true;

				// Determine if continue handshake
				if (!isHandshake) {
					return; // data sent
				}
			}
		}
	}

	/**
	 * Remaining cipher bytes for unwrap.
	 */
	private byte[] remainingCipherBytes = new byte[0];

	/**
	 * Received data from the Server and unwraps it.
	 * 
	 * @param isHandshake
	 *            Indicates if undertaking handshake.
	 * @return Unwrapped data.
	 */
	private byte[] doUnwrap(boolean isHandshake) throws IOException {

		ByteArrayOutputStream receivedData = new ByteArrayOutputStream();
		for (;;) {

			// Handle handshake
			boolean isUnwrap = false;
			HandshakeStatus handshakeStatus = this.clientEngine.getHandshakeStatus();
			switch (handshakeStatus) {

			case NEED_WRAP:
				byte[] received = receivedData.toByteArray();
				return received; // await request

			case NOT_HANDSHAKING:
				if (isHandshake) {
					return new byte[0]; // handshake established
				}
			case NEED_UNWRAP:
				isUnwrap = true;
				break;

			case NEED_TASK:

				// Indicate progress
				System.out.println("CLIENT: task");

				this.clientEngine.getDelegatedTask().run();
				break; // allow to undertake another tasks

			default:
				fail("Handshake status " + handshakeStatus);
			}

			// Determine if received and unwrap data
			if (isUnwrap) {

				// Obtain sizes for unwrapping
				SSLSession session = this.clientEngine.getSession();
				int packetBufferSize = session.getPacketBufferSize();
				int applicationBufferSize = session.getApplicationBufferSize();

				// Determine if receive data from server
				byte[] cipherData;
				if (this.remainingCipherBytes.length < packetBufferSize) {

					// Indicate progress
					System.out.println("CLIENT: receive more data");

					// Receive the data from the server
					this.runServerSelect();
					ByteArrayOutputStream received = new ByteArrayOutputStream();
					super.writeClientReceivedData(received);
					cipherData = received.toByteArray();
					assertNotNull("Ensure have cipher data", cipherData);

				} else {
					// Unwrap remaining data
					cipherData = new byte[0];
				}

				// Include remaining cipher data
				byte[] dataToUnwrap = new byte[this.remainingCipherBytes.length + cipherData.length];
				System.arraycopy(this.remainingCipherBytes, 0, dataToUnwrap, 0, this.remainingCipherBytes.length);
				System.arraycopy(cipherData, 0, dataToUnwrap, this.remainingCipherBytes.length, cipherData.length);

				// Ensure consume all cipher data
				int totalBytesConsumed = 0;
				Status status = Status.OK;
				boolean isRunOnce = true;
				UNWRAP_DATA: while (isRunOnce && (totalBytesConsumed < dataToUnwrap.length)) {
					isRunOnce = false;

					// Indicate progress
					System.out.println("CLIENT: unwrap");

					// Obtain the next buffer of cipher data
					int bytesToConsume = Math.min((dataToUnwrap.length - totalBytesConsumed), packetBufferSize);
					ByteBuffer cipherBuffer = ByteBuffer.wrap(dataToUnwrap, totalBytesConsumed, bytesToConsume);

					// Create buffer for plain data
					ByteBuffer plainBuffer = ByteBuffer.allocate(applicationBufferSize);

					// Unwrap the cipher data
					SSLEngineResult result = this.clientEngine.unwrap(cipherBuffer, plainBuffer);
					status = result.getStatus();
					switch (status) {
					case BUFFER_UNDERFLOW:
						// Need to receive further data
						break UNWRAP_DATA;
					case CLOSED:

						// Indicate progress
						System.out.println("CLIENT: closed");

						// No further data
						return null;

					case OK:
						break;
					default:
						fail("Should have unwrapped data: " + status);
					}

					// Keep track of bytes consumed
					int bytesConsumed = result.bytesConsumed();
					totalBytesConsumed += bytesConsumed;

					// Handle whether handshake
					handshakeStatus = this.clientEngine.getHandshakeStatus();
					switch (handshakeStatus) {

					case NEED_TASK:
					case NEED_WRAP:
						// Stop unwrapping data
						break UNWRAP_DATA;

					case NEED_UNWRAP:
					case NOT_HANDSHAKING:
						// Consume the bytes
						assertTrue("Should have consumed bytes in unwrap [handshake "
								+ this.clientEngine.getHandshakeStatus() + "]", (bytesConsumed > 0));

						// Obtain the data
						plainBuffer.flip();
						byte[] plainData = new byte[plainBuffer.remaining()];
						plainBuffer.get(plainData);
						receivedData.write(plainData);

						break;

					default:
						fail("Handshake status: " + handshakeStatus);
					}
				}

				// Keep track of remaining data
				this.remainingCipherBytes = new byte[dataToUnwrap.length - totalBytesConsumed];
				System.arraycopy(dataToUnwrap, totalBytesConsumed, this.remainingCipherBytes, 0,
						this.remainingCipherBytes.length);

				// Determine if continue handshake
				if (!isHandshake) {
					return receivedData.toByteArray();
				}
			}
		}
	}

}