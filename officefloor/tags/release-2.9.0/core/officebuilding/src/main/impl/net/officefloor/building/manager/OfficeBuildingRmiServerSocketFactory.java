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
	 * Creates the server {@link SSLContext}.
	 * 
	 * @param keyStoreContent
	 *            Content of the key store.
	 * @param keyStorePassword
	 *            Optional password to the key store. May be <code>null</code>.
	 * @return Server {@link SSLContext}.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createServerSslContext(byte[] keyStoreContent,
			String keyStorePassword) throws Exception {

		// Obtain the key store
		KeyStore keyStore = OfficeBuildingRmiClientSocketFactory
				.createKeyStore(keyStoreContent, keyStorePassword);

		// Create the Key Managers
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, (keyStorePassword == null ? null
				: keyStorePassword.toCharArray()));
		KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

		// Create and initialise the SSL context
		SSLContext context = OfficeBuildingRmiClientSocketFactory
				.createSslContext();
		context.init(keyManagers, null, null);

		// Return the context
		return context;
	}

	/**
	 * {@link KeyStore} content.
	 */
	private final byte[] keyStoreContent;

	/**
	 * {@link KeyStore} password.
	 */
	private final String keyStorePassword;

	/**
	 * {@link SSLSocketFactory}.
	 */
	private SSLSocketFactory socketFactory = null;

	/**
	 * Initiate.
	 * 
	 * @param keyStoreContent
	 *            {@link KeyStore} content.
	 * @param keyStorePassword
	 *            {@link KeyStore} password.
	 */
	public OfficeBuildingRmiServerSocketFactory(byte[] keyStoreContent,
			String keyStorePassword) {
		this.keyStoreContent = keyStoreContent;
		this.keyStorePassword = keyStorePassword;
	}

	/*
	 * ================== RMIServerSocketFactory =======================
	 */

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {

		// Ensure have the server socket factory
		SSLSocketFactory factory;
		synchronized (this) {

			// Lazy load the socket factory
			if (this.socketFactory == null) {
				try {
					SSLContext context = createServerSslContext(
							this.keyStoreContent, this.keyStorePassword);
					factory = context.getSocketFactory();
					this.socketFactory = context.getSocketFactory();
				} catch (Exception ex) {
					throw new IOException("Failed to create "
							+ SSLSocketFactory.class.getSimpleName(), ex);
				}
			}

			// Provide the factory for creating the socket
			factory = this.socketFactory;
		}

		// Create and return the socket
		final SSLSocketFactory sslSocketFactory = factory;
		ServerSocket socket = new ServerSocket(port) {
			@Override
			public Socket accept() throws IOException {
				Socket socket = super.accept();
				SSLSocket sslSocket = (SSLSocket) sslSocketFactory
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