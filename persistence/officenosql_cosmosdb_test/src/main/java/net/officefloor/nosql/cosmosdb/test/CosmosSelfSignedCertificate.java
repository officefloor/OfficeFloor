/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.nosql.cosmosdb.test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

/**
 * Allows use of self signed certificate for connecting to Cosmos.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosSelfSignedCertificate extends Configs {

	/**
	 * Avoid Open SSL due to Cosmos incompatibilities with Netty.
	 */
	public static void noOpenSsl() {
		System.setProperty("io.netty.handler.ssl.noOpenSsl", "true");
	}

	/**
	 * Initialises the {@link CosmosClientBuilder}.
	 * 
	 * @param clientBuilder {@link CosmosClientBuilder} to initialise.
	 * @param certificate   Certificate to Cosmos DB emulator.
	 * @throws Exception If fails to create {@link SslContext}.
	 */
	public static void initialise(CosmosClientBuilder clientBuilder, String certificate) throws Exception {
		Method configs = CosmosClientBuilder.class.getDeclaredMethod("configs", new Class[] { Configs.class });
		configs.setAccessible(true);
		configs.invoke(clientBuilder, new CosmosSelfSignedCertificate(certificate));
	}

	/**
	 * {@link SslContext}.
	 */
	private final SslContext sslContext;

	/**
	 * Initialise.
	 * 
	 * @param certificate Certificate to Cosmos DB emulator.
	 * @throws Exception If fails to create {@link SslContext}.
	 */
	private CosmosSelfSignedCertificate(String certificate) throws Exception {

		// Create the key store
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X509")
				.generateCertificate(new ByteArrayInputStream(certificate.getBytes()));
		keyStore.setCertificateEntry("Cosmos DB Emulator", x509Certificate);

		// Create the trust store for the certificate
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		// Create the SSL context
		this.sslContext = SslContextBuilder.forClient().sslProvider(SslProvider.JDK).trustManager(trustManagerFactory)
				.build();
	}

	/*
	 * ======================== Configs =======================
	 */

	@Override
	public SslContext getSslContext() {
		return this.sslContext;
	}

}
