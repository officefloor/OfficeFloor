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
package net.officefloor.plugin.socket.server.http.protocol;

import java.nio.charset.Charset;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpConversationImpl;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpManagedObjectImpl;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;

/**
 * HTTP {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCommunicationProtocol implements CommunicationProtocolSource,
		CommunicationProtocol {

	/**
	 * Name of property to determine if send stack trace on failure.
	 */
	public static final String PROPERTY_IS_SEND_STACK_TRACE_ON_FAILURE = "is.send.stack.trace.on.failure";

	/**
	 * Default value for property
	 * {@link #PROPERTY_IS_SEND_STACK_TRACE_ON_FAILURE}.
	 */
	public static final boolean DEFAULT_VALUE_IS_SEND_STACK_TRACE_ON_FAILURE = false;

	/**
	 * Flags whether to send the stack trace on a failure.
	 */
	private boolean isSendStackTraceOnFailure;

	/**
	 * Name of property to indicate the maximum number of {@link HttpHeader}
	 * instances per {@link HttpRequest}.
	 */
	public static final String PROPERTY_MAXIMUM_HTTP_REQUEST_HEADERS = "max.http.request.headers";

	/**
	 * Default value for the property
	 * {@link #PROPERTY_MAXIMUM_HTTP_REQUEST_HEADERS}.
	 */
	public static final int DEFAULT_VALUE_MAXIMUM_HTTP_REQUEST_HEADERS = 255;

	/**
	 * Maximum number of {@link HttpHeader} instances per {@link HttpRequest}.
	 */
	private int maximumHttpRequestHeaders;

	/**
	 * Property name for the maximum length in bytes of the {@link HttpRequest}
	 * body.
	 */
	public static final String PROPERTY_MAXIMUM_REQUEST_BODY_LENGTH = "max.http.request.body.length";

	/**
	 * Default value for property {@link #PROPERTY_MAXIMUM_REQUEST_BODY_LENGTH}.
	 */
	public static final long DEFAULT_VALUE_MAXIMUM_REQUEST_BODY_LENGTH = (1024 * 1024);

	/**
	 * Maximum length in bytes of the {@link HttpRequest} body.
	 */
	private long maximumRequestBodyLength;

	/**
	 * Property name for the connection timeout.
	 */
	public static final String PROPERTY_CONNECTION_TIMEOUT = "connection.timeout";

	/**
	 * Default value for property {@link #PROPERTY_CONNECTION_TIMEOUT}.
	 */
	public static final long DEFAULT_VALUE_CONNECTION_TIMEOUT = 5 * 60 * 1000;

	/**
	 * Timeout of the {@link Connection} in milliseconds.
	 */
	private long connectionTimeout;

	/**
	 * Property name for the maximum length of a text part for the
	 * {@link HttpRequest}.
	 */
	public static final String PROPERTY_MAXIMUM_TEXT_PART_LENGTH = "max.text.part.length";

	/**
	 * Default value for property {@link #PROPERTY_MAXIMUM_TEXT_PART_LENGTH}.
	 */
	public static final int DEFAULT_VALUE_MAXIMUM_TEXT_PART_LENGTH = 255;

	/**
	 * Maximum length of text part for {@link HttpRequest}.
	 */
	private int maxTextPartLength = 255;

	/**
	 * Send buffer size.
	 */
	private int sendBufferSize;

	/**
	 * Default {@link Charset}.
	 */
	private Charset defaultCharset;

	/**
	 * Flow index to handle processing {@link HttpRequest}.
	 */
	private int requestHandlingFlowIndex;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Services the {@link HttpRequest}.
	 * 
	 * @param handler
	 *            {@link HttpConnectionHandler}.
	 * @param httpManagedObject
	 *            {@link HttpManagedObject} for the {@link HttpRequest}.
	 */
	public void serviceHttpRequest(HttpConnectionHandler handler,
			HttpManagedObject httpManagedObject) {

		// Invoke processing of the HTTP managed object
		this.executeContext.invokeProcess(this.requestHandlingFlowIndex,
				httpManagedObject.getServerHttpConnection(), httpManagedObject,
				0, httpManagedObject.getEscalationHandler());
	}

	/*
	 * =================== CommunicationProtocolSource ======================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		// All properties are optional
	}

	@Override
	public CommunicationProtocol createCommunicationProtocol(
			MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = configurationContext
				.getManagedObjectSourceContext();

		// Obtain properties
		this.isSendStackTraceOnFailure = Boolean
				.parseBoolean(mosContext.getProperty(
						PROPERTY_IS_SEND_STACK_TRACE_ON_FAILURE,
						String.valueOf(DEFAULT_VALUE_IS_SEND_STACK_TRACE_ON_FAILURE)));
		this.maximumHttpRequestHeaders = Integer.parseInt(mosContext
				.getProperty(PROPERTY_MAXIMUM_HTTP_REQUEST_HEADERS, String
						.valueOf(DEFAULT_VALUE_MAXIMUM_HTTP_REQUEST_HEADERS)));
		this.maximumRequestBodyLength = Long.parseLong(mosContext.getProperty(
				PROPERTY_MAXIMUM_REQUEST_BODY_LENGTH,
				String.valueOf(DEFAULT_VALUE_MAXIMUM_REQUEST_BODY_LENGTH)));
		this.connectionTimeout = Long.parseLong(mosContext.getProperty(
				PROPERTY_CONNECTION_TIMEOUT,
				String.valueOf(DEFAULT_VALUE_CONNECTION_TIMEOUT)));
		this.maxTextPartLength = Integer.parseInt(mosContext.getProperty(
				PROPERTY_MAXIMUM_TEXT_PART_LENGTH,
				String.valueOf(DEFAULT_VALUE_MAXIMUM_TEXT_PART_LENGTH)));

		// Obtain context details
		this.sendBufferSize = protocolContext.getSendBufferSize();
		this.defaultCharset = protocolContext.getDefaultCharset();

		// Specify types
		configurationContext.setManagedObjectClass(HttpManagedObjectImpl.class);
		configurationContext.setObjectClass(ServerHttpConnection.class);

		// Provide the flow to handle the HTTP request
		this.requestHandlingFlowIndex = configurationContext
				.addFlow(ServerHttpConnection.class)
				.setLabel("HANDLE_HTTP_REQUEST").getIndex();

		// Ensure connection is cleaned up when process finished
		new CleanupTask().registerAsRecycleTask(mosContext, "cleanup");

		// Return this as the server
		return this;
	}

	/*
	 * ====================== CommunicationProtocol ============================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public HttpConnectionHandler createConnectionHandler(Connection connection) {
		HttpConversation conversation = new HttpConversationImpl(connection,
				this.sendBufferSize, this.defaultCharset,
				this.isSendStackTraceOnFailure);
		HttpRequestParser parser = new HttpRequestParserImpl(
				this.maximumHttpRequestHeaders, this.maxTextPartLength,
				this.maximumRequestBodyLength);
		return new HttpConnectionHandler(this, conversation, parser,
				this.connectionTimeout);
	}

}