/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.ssl.protocol;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests use of the {@link SSLEngine} to understand how to use it.
 *
 * @author Daniel Sagenschneider
 */
public class SSLEngineTest extends OfficeFrameTestCase {

	/**
	 * Test to understand how the {@link SSLEngine} works.
	 */
	public void testSSLEngine() throws NoSuchAlgorithmException, SSLException {

		SSLContext context = SSLContext.getDefault();

		// Create the server engine and buffers
		SSLEngine server = context.createSSLEngine("client", 10000);
		server.setUseClientMode(false);
		server.setEnabledCipherSuites(server.getSupportedCipherSuites());
		assertEquals("Incorrect server peer host", "client", server
				.getPeerHost());
		assertEquals("Incorrect server peer port", 10000, server.getPeerPort());
		System.out.println("=== SERVER ===");
		this.printDetails("Enabled protocols", server.getEnabledProtocols());
		this.printDetails("Enabled ciphers", server.getEnabledCipherSuites());
		int serverPacketBufferSize = server.getSession().getPacketBufferSize();
		int serverApplicationBufferSize = server.getSession()
				.getApplicationBufferSize();
		System.out.println("Packet buffer size: " + serverPacketBufferSize);
		System.out.println("Application buffer size: "
				+ serverApplicationBufferSize);
		ByteBuffer serverPacketIn = ByteBuffer.allocate(serverPacketBufferSize);
		ByteBuffer serverPacketOut = ByteBuffer
				.allocate(serverPacketBufferSize);
		ByteBuffer serverApplicationIn = ByteBuffer
				.allocate(serverApplicationBufferSize);
		ByteBuffer serverApplicationOut = ByteBuffer
				.allocate(serverApplicationBufferSize);

		// Create the client engine
		SSLEngine client = context.createSSLEngine("server", 443);
		client.setUseClientMode(true);
		client.setEnabledCipherSuites(client.getSupportedCipherSuites());
		assertEquals("Incorrect server peer host", "client", server
				.getPeerHost());
		assertEquals("Incorrect server peer port", 10000, server.getPeerPort());
		System.out.println("\n=== CLIENT ===");
		this.printDetails("Enabled protocols", server.getEnabledProtocols());
		this.printDetails("Enabled ciphers", server.getEnabledCipherSuites());
		int clientPacketBufferSize = client.getSession().getPacketBufferSize();
		int clientApplicationBufferSize = client.getSession()
				.getApplicationBufferSize();
		System.out.println("Packet buffer size: " + clientPacketBufferSize);
		System.out.println("Application buffer size: "
				+ clientApplicationBufferSize);
		ByteBuffer clientPacketIn = ByteBuffer.allocate(clientPacketBufferSize);
		ByteBuffer clientPacketOut = ByteBuffer
				.allocate(clientPacketBufferSize);
		ByteBuffer clientApplicationIn = ByteBuffer
				.allocate(clientApplicationBufferSize);
		ByteBuffer clientApplicationOut = ByteBuffer
				.allocate(clientApplicationBufferSize);

		// Check initial state
		assertEquals("Incorrect initial server hand shake status",
				HandshakeStatus.NOT_HANDSHAKING, server.getHandshakeStatus());
		assertEquals("Incorrect initial client hand shake status",
				HandshakeStatus.NOT_HANDSHAKING, client.getHandshakeStatus());

		// Start communication
		System.out.println("\n=== COMMUNICATION ===");

		// Start hand shake
		server.beginHandshake();
		client.beginHandshake();
		assertEquals("Ensure require client content",
				HandshakeStatus.NEED_UNWRAP, server.getHandshakeStatus());
		assertEquals("Ensure require provide content",
				HandshakeStatus.NEED_WRAP, client.getHandshakeStatus());

		// Do the handshake
		System.out.println("STARTING HANDSHAKE...");
		this.doWrap(client, clientApplicationOut, clientPacketOut);
		this.wireTransfer(clientPacketOut, serverPacketIn);

		this.doUnwrap(server, serverPacketIn, serverApplicationIn);
		this.doTask(server);
		this.doWrap(server, serverApplicationOut, serverPacketOut);
		this.wireTransfer(serverPacketOut, clientPacketIn);

		this.doUnwrap(client, clientPacketIn, clientApplicationIn);
		this.doTask(client);
		this.doWrap(client, clientApplicationOut, clientPacketOut);
		this.wireTransfer(clientPacketOut, serverPacketIn);

		this.doUnwrap(server, serverPacketIn, serverApplicationIn);
		this.doTask(server);
		this.doWrap(client, clientApplicationOut, clientPacketOut);
		this.wireTransfer(clientPacketOut, serverPacketIn);

		this.doUnwrap(server, serverPacketIn, serverApplicationIn);
		this.doWrap(client, clientApplicationOut, clientPacketOut);
		this.wireTransfer(clientPacketOut, serverPacketIn);

		this.doUnwrap(server, serverPacketIn, serverApplicationIn);
		this.doWrap(server, serverApplicationOut, serverPacketOut);
		this.wireTransfer(serverPacketOut, clientPacketIn);

		this.doUnwrap(client, clientPacketIn, clientApplicationIn);
		this.doWrap(server, serverApplicationOut, serverPacketOut);
		this.wireTransfer(serverPacketOut, clientPacketIn);

		this.doUnwrap(client, clientPacketIn, clientApplicationIn);
		System.out.println("\nFINISHED HANDSHAKE\n");

		// Ensure handshake is finished
		assertEquals("Incorrect initial server hand shake status",
				HandshakeStatus.NOT_HANDSHAKING, server.getHandshakeStatus());
		assertEquals("Incorrect initial client hand shake status",
				HandshakeStatus.NOT_HANDSHAKING, client.getHandshakeStatus());

		// Send some data from client to server
		System.out.println("\nSENDING DATA: client to server");
		final String msgClientToServer = "CLIENT -> SERVER";
		clientApplicationOut.put(msgClientToServer.getBytes());
		this.doWrap(client, clientApplicationOut, clientPacketOut);
		this.wireTransfer(clientPacketOut, serverPacketIn);
		this.doUnwrap(server, serverPacketIn, serverApplicationIn);
		serverApplicationIn.flip();
		String receivedMessage = new String(serverApplicationIn.array(),
				serverApplicationIn.position(), serverApplicationIn.limit());
		serverApplicationIn.clear();
		assertEquals("Incorrect client to server message", msgClientToServer,
				receivedMessage);
		System.out.println("Message received: " + receivedMessage);

		// Send some data from server to client
		System.out.println("\n\nSENDING DATA: server to client");
		final String msgServerToClient = "SERVER data for CLIENT";
		serverApplicationOut.put(msgServerToClient.getBytes());
		this.doWrap(server, serverApplicationOut, serverPacketOut);
		this.wireTransfer(serverPacketOut, clientPacketIn);
		this.doUnwrap(client, clientPacketIn, clientApplicationIn);
		clientApplicationIn.flip();
		receivedMessage = new String(clientApplicationIn.array(),
				clientApplicationIn.position(), clientApplicationIn.limit());
		clientApplicationIn.clear();
		assertEquals("Incorrect server to client message", msgServerToClient,
				receivedMessage);
		System.out.println("Message received: " + receivedMessage);

		// Have server close connection
		System.out.println("\n\nCLOSING CONNECTION (by server)");
		System.out.print("SERVER closeOutbound HS:"
				+ server.getHandshakeStatus());
		server.closeOutbound();
		System.out.println(" -> HS:" + server.getHandshakeStatus());
		this.doWrap(server, serverApplicationOut, serverPacketOut);
		this.wireTransfer(serverPacketOut, clientPacketIn);
		this.doUnwrap(client, clientPacketIn, clientApplicationIn);
		this.doWrap(client, clientApplicationOut, clientPacketOut);
		this.wireTransfer(clientPacketOut, serverPacketIn);
		this.doUnwrap(server, serverPacketIn, serverApplicationIn);
	}

	/**
	 * Does an unwrap providing details.
	 */
	private SSLEngineResult doUnwrap(SSLEngine engine, ByteBuffer src,
			ByteBuffer dst) throws SSLException {
		src.flip();
		this.printSide(engine);
		System.out.print(" unwrap HS:" + engine.getHandshakeStatus() + " (s"
				+ src.position() + "-" + src.limit() + ",d" + dst.position()
				+ "-" + dst.limit() + ")");
		SSLEngineResult result = engine.unwrap(src, dst);
		System.out.print(" -> HS:" + result.getHandshakeStatus() + " (s"
				+ src.position() + "-" + src.limit() + ",d" + dst.position()
				+ "-" + dst.limit() + ")");
		this.printResult(engine, result);
		src.clear();
		System.out
				.print(" (clear s" + src.position() + "-" + src.limit() + ")");
		System.out.println();
		return result;
	}

	/**
	 * Does a wrap providing details.
	 */
	private SSLEngineResult doWrap(SSLEngine engine, ByteBuffer src,
			ByteBuffer dst) throws SSLException {
		src.flip();
		this.printSide(engine);
		System.out.print(" wrap HS:" + engine.getHandshakeStatus() + " (s"
				+ src.position() + "-" + src.limit() + ",d" + dst.position()
				+ "-" + dst.limit() + ")");
		SSLEngineResult result = engine.wrap(src, dst);
		System.out.print(" -> HS:" + result.getHandshakeStatus() + " (s"
				+ src.position() + "-" + src.limit() + ",d" + dst.position()
				+ "-" + dst.limit() + ")");
		this.printResult(engine, result);
		src.clear();
		System.out
				.print(" (clear s" + src.position() + "-" + src.limit() + ")");
		System.out.println();
		return result;
	}

	/**
	 * Does a task providing details.
	 */
	private void doTask(SSLEngine engine) {
		this.printSide(engine);
		Runnable task = engine.getDelegatedTask();
		System.out.print(" task " + task.getClass().getSimpleName() + " HS:"
				+ engine.getHandshakeStatus());
		task.run();
		System.out.print(" -> HS:" + engine.getHandshakeStatus());
		System.out.println();
	}

	/**
	 * Does a wire transfer providing details.
	 */
	private void wireTransfer(ByteBuffer src, ByteBuffer dst) {
		src.flip();
		System.out.print("TRANSFER [s" + src.position() + "-" + src.limit()
				+ ",d" + dst.position() + "-" + dst.limit() + "]");
		dst.put(src);
		System.out.print(" -> [s" + src.position() + "-" + src.limit() + ",d"
				+ dst.position() + "-" + dst.limit() + "]");
		src.clear();
		System.out
				.print(" (clear s" + src.position() + "-" + src.limit() + ")");
		System.out.println("\n");
	}

	/**
	 * Prints the side of the communication the {@link SSLEngine} is on.
	 */
	private void printSide(SSLEngine engine) {
		String side;
		if ("server".equalsIgnoreCase(engine.getPeerHost())) {
			side = "CLIENT";
		} else if ("client".equalsIgnoreCase(engine.getPeerHost())) {
			side = "SERVER";
		} else {
			throw new IllegalArgumentException("Unknown side of connection");
		}
		System.out.print(side);
	}

	/**
	 * Prints details of the {@link SSLEngineResult}.
	 */
	private void printResult(SSLEngine engine, SSLEngineResult result) {
		System.out.print(" (S:" + result.getStatus() + " bc"
				+ result.bytesConsumed() + " bp" + result.bytesProduced()
				+ " ps" + engine.getSession().getPacketBufferSize() + ", as"
				+ engine.getSession().getApplicationBufferSize() + ")");
	}

	/**
	 * Prints details.
	 *
	 * @param type
	 *            Type of details.
	 * @param values
	 *            Values for the details.
	 */
	private void printDetails(String type, String... values) {
		System.out.print(type + ": ");
		for (String value : values) {
			System.out.print(value + " ");
		}
		System.out.println();
	}
}