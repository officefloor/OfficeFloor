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
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;
import net.officefloor.plugin.socket.server.ssl.protocol.SslTaskWork.SslTaskDependencies;

/**
 * SSL {@link CommunicationProtocolSource} that wraps another
 * {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslCommunicationProtocol implements CommunicationProtocolSource,
		CommunicationProtocol, SslTaskExecutor {

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
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Flow index to run the SSL tasks.
	 */
	private int sslTaskFlowIndex;

	/**
	 * Initiate.
	 * 
	 * @param wrappedCommunicationProtocolSource
	 *            {@link CommunicationProtocolSource} to be wrapped with this
	 *            SSL {@link CommunicationProtocolSource}.
	 */
	public SslCommunicationProtocol(
			CommunicationProtocolSource wrappedCommunicationProtocolSource) {
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
	public CommunicationProtocol createCommunicationProtocol(
			MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = configurationContext
				.getManagedObjectSourceContext();

		// Obtain the send buffer size
		this.sendBufferSize = protocolContext.getSendBufferSize();

		// Create the communication protocol to wrap
		this.wrappedCommunicationProtocol = this.wrappedCommunicationProtocolSource
				.createCommunicationProtocol(configurationContext,
						protocolContext);

		// Obtain the SSL Engine Source
		String sslEngineSourceClassName = mosContext.getProperty(
				PROPERTY_SSL_ENGINE_SOURCE, null);
		if (sslEngineSourceClassName == null) {
			// Use default SSL Engine source
			this.sslEngineSource = new DefaultSslEngineSource();
		} else {
			// Instantiate specified source
			this.sslEngineSource = (SslEngineSource) mosContext.loadClass(
					sslEngineSourceClassName).newInstance();
		}

		// Initialise the SSL Engine Source
		this.sslEngineSource.init(mosContext);

		// Create the flow to execute the SSL tasks
		this.sslTaskFlowIndex = configurationContext.addFlow(Runnable.class)
				.setLabel("SSL_TASKS").getIndex();
		SslTaskWork sslTaskExecution = new SslTaskWork();
		ManagedObjectWorkBuilder<SslTaskWork> work = mosContext.addWork(
				"SSL_TASK_EXECUTOR", sslTaskExecution);
		ManagedObjectTaskBuilder<SslTaskDependencies, None> task = work
				.addTask("SSL_TASK_EXECUTOR", sslTaskExecution);
		task.linkParameter(SslTaskDependencies.TASK, Runnable.class);
		task.setTeam("SSL_TASKS");
		mosContext.linkProcess(this.sslTaskFlowIndex, "SSL_TASK_EXECUTOR",
				"SSL_TASK_EXECUTOR");

		// Return this wrapping the server socket handler
		return this;
	}

	/*
	 * =================== CommunicationProtocol ==========================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext) {

		// Store execution context for executing SSL tasks
		this.executeContext = executeContext;

		// Provide execution context to wrapped server
		this.wrappedCommunicationProtocol
				.setManagedObjectExecuteContext(executeContext);
	}

	@Override
	public SslConnectionHandler createConnectionHandler(Connection connection) {

		// Obtain the remote connection details
		InetSocketAddress remoteAddress = connection.getRemoteAddress();
		String remoteHost = remoteAddress.getHostName();
		int remotePort = remoteAddress.getPort();

		// Create the server SSL engine
		SSLEngine engine = this.sslEngineSource.createSslEngine(remoteHost,
				remotePort);
		engine.setUseClientMode(false); // Always in server mode

		// Create the SSL connection wrapping the connection
		SslConnectionHandler connectionHandler = new SslConnectionHandler(
				connection, engine, this, this.sendBufferSize,
				this.wrappedCommunicationProtocol);

		// Return the SSL connection handler
		return connectionHandler;
	}

	/*
	 * ===================== SslTaskExecutor ==================================
	 */

	@Override
	public void beginTask(Runnable task) {
		// Invoke process to execute the task
		this.executeContext.invokeProcess(this.sslTaskFlowIndex, task, null, 0);
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