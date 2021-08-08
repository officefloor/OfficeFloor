/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
