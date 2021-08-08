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

import java.lang.reflect.Method;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

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
	 * @throws Exception If fails to create {@link SslContext}.
	 */
	public static void initialise(CosmosClientBuilder clientBuilder) throws Exception {
		Method configs = CosmosClientBuilder.class.getDeclaredMethod("configs", new Class[] { Configs.class });
		configs.setAccessible(true);
		configs.invoke(clientBuilder, new CosmosSelfSignedCertificate());
	}

	/**
	 * {@link SslContext}.
	 */
	private final SslContext sslContext;

	/**
	 * Initialise.
	 * 
	 * @throws Exception If fails to create {@link SslContext}.
	 */
	private CosmosSelfSignedCertificate() throws Exception {
		this.sslContext = SslContextBuilder.forClient().sslProvider(SslProvider.JDK)
				.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	}

	/*
	 * ======================== Configs =======================
	 */

	@Override
	public SslContext getSslContext() {
		return this.sslContext;
	}

}
