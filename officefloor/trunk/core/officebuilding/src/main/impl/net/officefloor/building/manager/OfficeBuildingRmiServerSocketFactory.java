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
package net.officefloor.building.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * {@link OfficeBuildingManager} {@link RMIServerSocketFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingRmiServerSocketFactory implements
		RMIServerSocketFactory {

	/**
	 * Obtains the {@link KeyStore} content.
	 * 
	 * @param keyStore
	 *            Location of the {@link KeyStore}.
	 * @return {@link KeyStore} content.
	 * @throws IOException
	 *             If fails to load {@link KeyStore} content.
	 */
	public static byte[] getKeyStoreContent(File keyStore) throws IOException {
		InputStream input = new FileInputStream(keyStore);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int value = input.read(); value != -1; value = input.read()) {
			buffer.write(value);
		}
		input.close();
		return buffer.toByteArray();
	}

	/**
	 * {@link SSLSocketFactory}.
	 */
	private final SSLSocketFactory socketFactory;

	/**
	 * Initiate.
	 * 
	 * @param sslProtocol
	 *            {@link SSLContext} protocol.
	 * @param sslAlgorithm
	 *            {@link KeyManager} algorithm.
	 * @param keyStoreContent
	 *            {@link KeyStore} content.
	 * @param keyStorePassword
	 *            {@link KeyStore} password. May be <code>null</code> if not
	 *            required.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	public OfficeBuildingRmiServerSocketFactory(String sslProtocol,
			String sslAlgorithm, byte[] keyStoreContent, String keyStorePassword)
			throws Exception {

		// Obtain the key store
		KeyStore keyStore = OfficeBuildingRmiClientSocketFactory
				.createKeyStore(keyStoreContent, keyStorePassword);

		// Create the Key Managers
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(sslAlgorithm);
		keyManagerFactory.init(keyStore, (keyStorePassword == null ? null
				: keyStorePassword.toCharArray()));
		KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

		// Create and initialise the SSL context
		SSLContext context = SSLContext.getInstance(sslProtocol);
		context.init(keyManagers, null, null);

		// Create the socket factory
		this.socketFactory = context.getSocketFactory();
	}

	/*
	 * ================== RMIServerSocketFactory =======================
	 */

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {

		// Create and return the socket
		ServerSocket socket = new ServerSocket(port) {
			@Override
			public Socket accept() throws IOException {
				Socket socket = super.accept();
				SSLSocket sslSocket = (SSLSocket) OfficeBuildingRmiServerSocketFactory.this.socketFactory
						.createSocket(socket, socket.getInetAddress()
								.getHostName(), socket.getPort(), true);
				sslSocket.setUseClientMode(false);
				sslSocket.setReuseAddress(true);
				return sslSocket;
			}
		};
		socket.setReuseAddress(true);
		return socket;
	}

}