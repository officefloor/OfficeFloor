/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.ssl;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;

/**
 * <p>
 * OfficeFloor default {@link SslContextSource}.
 * <p>
 * <b>This should NOT be used within production.</b> The purpose is to allow
 * testing of HTTPS communication without needing manual {@link KeyStore} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDefaultSslContextSource implements SslContextSource {

	/**
	 * Property to specify the SSL protocol to use.
	 */
	public static final String PROPERTY_SSL_PROTOCOL = "ssl.protocol";

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloorDefaultSslContextSource.class.getName());

	/**
	 * Protocol used.
	 */
	private static String protocolUsed = null;

	/**
	 * Creates the {@link OfficeFloor} default server {@link SSLContext}.
	 * 
	 * @param sslProtocol SSL protocol. May be <code>null</code>.
	 * @return {@link OfficeFloor} default server {@link SSLContext}.
	 * @throws Exception If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createServerSslContext(String sslProtocol) throws Exception {

		// Obtain the key store
		KeyStore keyStore = createOfficeFloorDefaultKeyStore();

		// Create the Key Managers
		String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
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
	 * @param sslProtocol SSL protocol. May be <code>null</code>.
	 * @return {@link OfficeFloor} default client {@link SSLContext}.
	 * @throws Exception If fails to create the {@link SSLContext}.
	 */
	public static SSLContext createClientSslContext(String sslProtocol) throws Exception {

		// Obtain the key store
		KeyStore keyStore = createOfficeFloorDefaultKeyStore();

		// Create the Trust Managers
		String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
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
	 * @param sslProtocol SSL protocol. May be <code>null</code>.
	 * @return {@link SSLContext}.
	 * @throws Exception If fails to create the {@link SSLContext}.
	 */
	private static SSLContext createSslContext(String sslProtocol) throws Exception {

		// Determine if provided SSL protocol
		if (sslProtocol != null) {
			// Create and return using the provided SSL protocol
			return SSLContext.getInstance(sslProtocol);
		}

		// Try finding an available protocol from default protocols
		synchronized (OfficeFloorDefaultSslContextSource.class) {
			if (protocolUsed != null) {
				// Protocol decided, so continue use
				return SSLContext.getInstance(protocolUsed);

			} else {
				// Determine the protocol (reverse order as prefer TLS over SSL)
				String[] protocols = SSLContext.getDefault().getSupportedSSLParameters().getProtocols();
				Arrays.sort(protocols, (a, b) -> -1 * String.CASE_INSENSITIVE_ORDER.compare(a, b));
				if (protocols != null) {
					for (String protocol : protocols) {
						try {
							// Attempt to create and return using SSL protocol
							SSLContext sslContext = SSLContext.getInstance(protocol);

							// Context created, so flag as the protocol used
							protocolUsed = protocol;

							// Return the SSL context
							return sslContext;
						} catch (Throwable ex) {
							// Ignore and try next protocol
						}
					}
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
	 * @throws Exception If fails to create the {@link KeyStore}.
	 */
	private static KeyStore createOfficeFloorDefaultKeyStore() throws Exception {

		// Obtain the default key store content
		String officeFloorDefaultKeysPath = OfficeFloorDefaultSslContextSource.class.getPackage().getName().replace('.',
				'/') + "/OfficeFloorDefault.jks";
		InputStream officeFloorDefaultKeys = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(officeFloorDefaultKeysPath);
		if (officeFloorDefaultKeys == null) {
			throw new IllegalStateException(
					"Unable to locate default OfficeFloor key/trust store " + officeFloorDefaultKeysPath);
		}

		// Initialise with default key store
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(officeFloorDefaultKeys, "Changeit".toCharArray());

		// Return the key store
		return keyStore;
	}

	/*
	 * ============== SslContextSource ==========================
	 */

	@Override
	public SSLContext createSslContext(SourceContext context) throws Exception {

		// Obtain the SSL protocol
		String sslProtocol = context.getProperty(PROPERTY_SSL_PROTOCOL, null);

		// Create and return the SSL Context
		SSLContext sslContext = createServerSslContext(sslProtocol);

		// Indicate loading generic OfficeFloor key store
		if ((!(context.isLoadingType())) && (LOGGER.isLoggable(Level.INFO))) {
			LOGGER.log(Level.WARNING,
					"Using default OfficeFloor Key Store. This should only be used for testing and NEVER in production.");
			LOGGER.log(Level.INFO, "Using SSL protocol " + sslContext.getProtocol());
		}

		// Return the SSL context
		return sslContext;
	}

}
