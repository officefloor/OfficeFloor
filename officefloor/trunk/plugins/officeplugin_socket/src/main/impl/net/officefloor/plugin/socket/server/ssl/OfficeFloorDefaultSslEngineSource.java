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
package net.officefloor.plugin.socket.server.ssl;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * <p>
 * OfficeFloor default {@link SslEngineSource}.
 * <p>
 * <b>This should NOT be used within production.</b> The purpose is to allow
 * testing of HTTPS communication without needing manual {@link KeyStore} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDefaultSslEngineSource implements SslEngineSource {

	/**
	 * Property to specify the SSL protocol to use.
	 */
	public static final String PROPERTY_SSL_PROTOCOL = "ssl.protocol";

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(OfficeFloorDefaultSslEngineSource.class.getName());

	/**
	 * Creates the {@link OfficeFloor} default server {@link SSLContext}.
	 * 
	 * @param sslProtocol
	 *            SSL protocol. May be <code>null</code>.
	 * @return {@link OfficeFloor} default server {@link SSLContext}.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createServerSslContext(String sslProtocol)
			throws Exception {

		// Obtain the key store
		KeyStore keyStore = createOfficeFloorDefaultKeyStore();

		// Create the Key Managers
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, "Changeit".toCharArray());
		KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

		// Create and initialise the SSL context
		SSLContext context = createSslContext(sslProtocol);
		context.init(keyManagers, null, null);

		// Return the context
		return context;
	}

	/**
	 * Creates the {@link OfficeFloor} default client {@link SSLContext}.
	 * 
	 * @param sslProtocol
	 *            SSL protocol. May be <code>null</code>.
	 * @return {@link OfficeFloor} default client {@link SSLContext}.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createClientSslContext(String sslProtocol)
			throws Exception {

		// Obtain the key store
		KeyStore keyStore = createOfficeFloorDefaultKeyStore();

		// Create the Trust Managers
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

		// Create and initialise the SSL context
		SSLContext context = createSslContext(sslProtocol);
		context.init(null, trustManagers, null);

		// Return the context
		return context;
	}

	/**
	 * Creates a new uninitialised {@link SSLContext}.
	 * 
	 * @param sslProtocol
	 *            SSL protocol. May be <code>null</code>.
	 * @return {@link SSLContext}.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext}.
	 */
	private static SSLContext createSslContext(String sslProtocol)
			throws Exception {

		// Determine if provided SSL protocol
		if (sslProtocol != null) {
			// Create and return using the provided SSL protocol
			return SSLContext.getInstance(sslProtocol);
		}

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
	 * Creates the {@link OfficeFloor} default {@link KeyStore}.
	 * 
	 * @return {@link OfficeFloor} default {@link KeyStore}.
	 * @throws Exception
	 *             If fails to create the {@link KeyStore}.
	 */
	private static KeyStore createOfficeFloorDefaultKeyStore() throws Exception {

		// Obtain the default key store content
		String officeFloorDefaultKeysPath = OfficeFloorDefaultSslEngineSource.class
				.getPackage().getName().replace('.', '/')
				+ "/OfficeFloorDefault.jks";
		InputStream officeFloorDefaultKeys = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(officeFloorDefaultKeysPath);
		if (officeFloorDefaultKeys == null) {
			throw new IllegalStateException(
					"Unable to locate default OfficeFloor key/trust store "
							+ officeFloorDefaultKeysPath);
		}

		// Initialise with default key store
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(officeFloorDefaultKeys, "Changeit".toCharArray());

		// Return the key store
		return keyStore;
	}

	/**
	 * {@link SSLContext}.
	 */
	private SSLContext sslContext;

	/*
	 * ============== SslEngineConfigurator ==========================
	 */

	@Override
	public void init(SourceProperties properties) throws Exception {

		// Indicate loading generic OfficeFloor key store
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(
					Level.INFO,
					"Using default OfficeFloor Key Store. "
							+ "This should only be used for testing and NEVER in production.");
		}

		// Obtain the SSL protocol
		String sslProtocol = properties
				.getProperty(PROPERTY_SSL_PROTOCOL, null);

		// Create the SSL Context
		this.sslContext = createServerSslContext(sslProtocol);
	}

	@Override
	public SSLEngine createSslEngine(String peerHost, int peerPort) {
		return this.sslContext.createSSLEngine(peerHost, peerPort);
	}

}