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
package net.officefloor.plugin.socket.server.ssl.protocol;

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
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslFunction.SslTaskDependencies;

/**
 * SSL {@link CommunicationProtocolSource} that wraps another
 * {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslCommunicationProtocol implements CommunicationProtocolSource, CommunicationProtocol {

	/**
	 * Property to obtain the optional {@link SslEngineSource} to provide the
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
	 * {@link SslEngineSource}.
	 */
	private SslEngineSource sslEngineSource;

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

		// Obtain the SSL Engine Source
		String sslEngineSourceClassName = mosContext.getProperty(PROPERTY_SSL_ENGINE_SOURCE, null);
		if (sslEngineSourceClassName == null) {
			// Use default SSL Engine source
			this.sslEngineSource = new DefaultSslEngineSource();
		} else {
			// Instantiate specified source
			this.sslEngineSource = (SslEngineSource) mosContext.loadClass(sslEngineSourceClassName).newInstance();
		}

		// Initialise the SSL Engine Source
		this.sslEngineSource.init(mosContext);

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
		SSLEngine engine = this.sslEngineSource.createSslEngine(remoteHost, remotePort);
		engine.setUseClientMode(false); // Always in server mode

		// Create the SSL connection wrapping the connection
		SslConnectionHandler connectionHandler = new SslConnectionHandler(connection, engine, this.sendBufferSize,
				this.wrappedCommunicationProtocol, executeContext, this.sslRunnableFlowIndex);

		// Return the SSL connection handler
		return connectionHandler;
	}

	/**
	 * {@link SslEngineSource} that uses the default {@link SSLContext}.
	 */
	private static class DefaultSslEngineSource implements SslEngineSource {

		/**
		 * {@link SSLContext}.
		 */
		private SSLContext sslContext;

		/*
		 * ================= SslEngineSource ==========================
		 */

		@Override
		public void init(SourceContext context) throws Exception {
			this.sslContext = SSLContext.getDefault();
		}

		@Override
		public SSLEngine createSslEngine(String peerHost, int peerPort) {
			return this.sslContext.createSSLEngine(peerHost, peerPort);
		}
	}

}