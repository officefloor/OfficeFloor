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

/**
 * {@link OfficeBuildingManager} {@link RMIClientSocketFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingRmiClientSocketFactory implements
		RMIClientSocketFactory, Serializable {

	/**
	 * Creates the client {@link SSLContext}.
	 * 
	 * @param keyStoreContent
	 *            Content of the key store.
	 * @param keyStorePassword
	 *            Optional password to the key store. May be <code>null</code>.
	 * @return Client {@link SSLContext}.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createClientSslContext(byte[] keyStoreContent,
			String keyStorePassword) throws Exception {

		// Obtain the key store
		KeyStore keyStore = createKeyStore(keyStoreContent, keyStorePassword);

		// Create the Trust Managers
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

		// Create and initialise the SSL context
		SSLContext context = createSslContext();
		context.init(null, trustManagers, null);

		// Return the context
		return context;
	}

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
	 * Creates a new uninitialised {@link SSLContext}.
	 * 
	 * @return {@link SSLContext}.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createSslContext() throws Exception {

		// Try finding an available protocol from default protocols
		String[] protocols = SSLContext.getDefault()
				.getSupportedSSLParameters().getProtocols();
		if (protocols != null) {
			for (String protocol : protocols) {
				try {
					// Attempt to create and return using SSL protocol
					return SSLContext.getInstance(protocol);
				} catch (Exception ex) {
					// Ignore and try next protocol
				}
			}
		}

		// As here, no SSL protocols available
		throw new IllegalStateException("No SSL protocols available");
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
	 * {@link SocketFactory}.
	 */
	private transient SocketFactory socketFactory = null;

	/**
	 * Initiate.
	 * 
	 * @param keyStoreContent
	 *            {@link KeyStore} content.
	 * @param keyStorePassword
	 *            {@link KeyStore} password.
	 */
	public OfficeBuildingRmiClientSocketFactory(byte[] keyStoreContent,
			String keyStorePassword) {
		this.keyStoreContent = keyStoreContent;
		this.keyStorePassword = keyStorePassword;
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
					SSLContext context = createClientSslContext(
							this.keyStoreContent, this.keyStorePassword);
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