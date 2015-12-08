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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 * <p>
 * {@link OfficeBuildingManager} {@link RMIClientSocketFactory}.
 * <p>
 * Extends {@link SslRMIClientSocketFactory} to enable use in JConsole
 * connections.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingRmiClientSocketFactory extends
		SslRMIClientSocketFactory implements RMIClientSocketFactory,
		Serializable {

	/**
	 * Creates the {@link KeyStore}.
	 * 
	 * @param keyStoreContent
	 *            Content of the key store.
	 * @param keyStorePassword
	 *            Optional password to the key store. May be <code>null</code>.
	 * @return {@link KeyStore}.
	 * @throws Exception
	 *             If fails to create the {@link KeyStore}.
	 */
	public static KeyStore createKeyStore(byte[] keyStoreContent,
			String keyStorePassword) throws Exception {

		// Create and initialise with key store
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(
				new ByteArrayInputStream(keyStoreContent),
				(keyStorePassword == null ? null : keyStorePassword
						.toCharArray()));

		// Return the key store
		return keyStore;
	}

	/**
	 * {@link SSLContext} protocol.
	 */
	private final String sslProtocol;

	/**
	 * {@link TrustManager} algorithm.
	 */
	private final String sslAlgorithm;

	/**
	 * {@link KeyStore} content.
	 */
	private final byte[] trustStoreContent;

	/**
	 * {@link KeyStore} password.
	 */
	private final String trustStorePassword;

	/**
	 * {@link SocketFactory}.
	 */
	private transient SocketFactory socketFactory = null;

	/**
	 * Initiate.
	 * 
	 * @param sslProtocol
	 *            {@link SSLContext} protocol.
	 * @param sslAlgorithm
	 *            {@link TrustManager} algorithm.
	 * @param trustStoreContent
	 *            {@link KeyStore} content.
	 * @param trustStorePassword
	 *            {@link KeyStore} password.
	 */
	public OfficeBuildingRmiClientSocketFactory(String sslProtocol,
			String sslAlgorithm, byte[] trustStoreContent,
			String trustStorePassword) {
		this.sslProtocol = sslProtocol;
		this.sslAlgorithm = sslAlgorithm;
		this.trustStoreContent = trustStoreContent;
		this.trustStorePassword = trustStorePassword;
	}

	/*
	 * ==================== RMIClientSocketFactory ======================
	 */

	@Override
	public Socket createSocket(String host, int port) throws IOException {

		// Ensure have the client socket factory
		SocketFactory factory;
		synchronized (this) {

			// Lazy load the socket factory
			if (this.socketFactory == null) {
				try {
					// Obtain the trust store
					KeyStore trustStore = createKeyStore(
							this.trustStoreContent, this.trustStorePassword);

					// Create the Trust Managers
					TrustManagerFactory trustManagerFactory = TrustManagerFactory
							.getInstance(this.sslAlgorithm);
					trustManagerFactory.init(trustStore);
					TrustManager[] trustManagers = trustManagerFactory
							.getTrustManagers();

					// Create and initialise the SSL context
					SSLContext context = SSLContext
							.getInstance(this.sslProtocol);
					context.init(null, trustManagers, null);

					// Create the socket factory
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
		Socket socket = factory.createSocket(host, port);
		return socket;
	}

}