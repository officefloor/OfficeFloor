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
package net.officefloor.server.ssl.protocol;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.protocol.CommunicationProtocol;
import net.officefloor.server.http.protocol.CommunicationProtocolContext;
import net.officefloor.server.http.protocol.CommunicationProtocolSource;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.ssl.SslContextSource;
import net.officefloor.server.ssl.protocol.SslFunction.SslTaskDependencies;

/**
 * SSL {@link CommunicationProtocolSource} that wraps another
 * {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslCommunicationProtocol implements CommunicationProtocolSource, CommunicationProtocol {

	/**
	 * Property to obtain the optional {@link SslContextSource} to provide the
	 * {@link SSLEngine} instances.
	 */
	public static final String PROPERTY_SSL_ENGINE_SOURCE = "ssl.engine.source";

	/**
	 * Wrapped {@link CommunicationProtocolSource}.
	 */
	private final CommunicationProtocolSource wrappedCommunicationProtocolSource;

	/**
	 * Wrapped {@link CommunicationProtocol}.
	 */
	private CommunicationProtocol wrappedCommunicationProtocol;

	/**
	 * {@link SSLContext}.
	 */
	private SSLContext sslContext;

	/**
	 * Send buffer size.
	 */
	private int sendBufferSize;

	/**
	 * Flow index to run the SSL {@link Runnable}.
	 */
	private int sslRunnableFlowIndex;

	/**
	 * Initiate.
	 * 
	 * @param wrappedCommunicationProtocolSource
	 *            {@link CommunicationProtocolSource} to be wrapped with this
	 *            SSL {@link CommunicationProtocolSource}.
	 */
	public SslCommunicationProtocol(CommunicationProtocolSource wrappedCommunicationProtocolSource) {
		this.wrappedCommunicationProtocolSource = wrappedCommunicationProtocolSource;
	}

	/*
	 * =================== CommunicationProtocolSource ========================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		// Load wrapped communication specification
		this.wrappedCommunicationProtocolSource.loadSpecification(context);
	}

	@Override
	public CommunicationProtocol createCommunicationProtocol(MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = configurationContext.getManagedObjectSourceContext();

		// Obtain the send buffer size
		this.sendBufferSize = protocolContext.getSendBufferSize();

		// Create the communication protocol to wrap
		this.wrappedCommunicationProtocol = this.wrappedCommunicationProtocolSource
				.createCommunicationProtocol(configurationContext, protocolContext);

		// Obtain the SSL Context
		String sslContextSourceClassName = mosContext.getProperty(PROPERTY_SSL_ENGINE_SOURCE, null);
		SslContextSource sslContextSource;
		if (sslContextSourceClassName == null) {
			// Use default SSL Engine source
			sslContextSource = new DefaultSslContextSource();
		} else {
			// Instantiate specified source
			sslContextSource = (SslContextSource) mosContext.loadClass(sslContextSourceClassName).newInstance();
		}
		this.sslContext = sslContextSource.createSslContext(mosContext);

		// Create the flow to execute the SSL tasks
		this.sslRunnableFlowIndex = configurationContext.addFlow(Runnable.class).setLabel("SSL_RUNNABLE").getIndex();
		SslFunction sslFunctionExecution = new SslFunction();
		ManagedObjectFunctionBuilder<SslTaskDependencies, None> function = mosContext
				.addManagedFunction("SSL_RUNNABLE_EXECUTOR", sslFunctionExecution);
		function.linkParameter(SslTaskDependencies.RUNNABLE, Runnable.class);
		function.setResponsibleTeam("SSL");
		mosContext.getFlow(this.sslRunnableFlowIndex).linkFunction("SSL_RUNNABLE_EXECUTOR");

		// Return this wrapping the server socket handler
		return this;
	}

	/*
	 * =================== CommunicationProtocol ==========================
	 */

	@Override
	public SslConnectionHandler createConnectionHandler(Connection connection,
			ManagedObjectExecuteContext<Indexed> executeContext) {

		// Obtain the remote connection details
		InetSocketAddress remoteAddress = connection.getRemoteAddress();
		String remoteHost = remoteAddress.getHostName();
		int remotePort = remoteAddress.getPort();

		// Create the server SSL engine
		SSLEngine engine = this.sslContext.createSSLEngine(remoteHost, remotePort);
		engine.setUseClientMode(false); // Always in server mode

		// Create the SSL connection wrapping the connection
		SslConnectionHandler connectionHandler = new SslConnectionHandler(connection, engine, this.sendBufferSize,
				this.wrappedCommunicationProtocol, executeContext, this.sslRunnableFlowIndex);

		// Return the SSL connection handler
		return connectionHandler;
	}

	/**
	 * {@link SslContextSource} providing the default {@link SSLContext}.
	 */
	private static class DefaultSslContextSource implements SslContextSource {

		@Override
		public SSLContext createSslContext(SourceContext context) throws Exception {
			return SSLContext.getDefault();
		}
	}

}