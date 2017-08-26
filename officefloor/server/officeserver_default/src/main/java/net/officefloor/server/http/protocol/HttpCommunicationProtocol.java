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
package net.officefloor.server.http.protocol;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * HTTP {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCommunicationProtocol implements CommunicationProtocolSource, CommunicationProtocol {

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
	 * Property name for the maximum length of a text part for the
	 * {@link HttpRequest}.
	 */
	public static final String PROPERTY_MAXIMUM_TEXT_PART_LENGTH = "max.text.part.length";

	/**
	 * Default value for property {@link #PROPERTY_MAXIMUM_TEXT_PART_LENGTH}.
	 */
	public static final int DEFAULT_VALUE_MAXIMUM_TEXT_PART_LENGTH = 1024;

	/**
	 * Maximum length of text part for {@link HttpRequest}.
	 */
	private int maxTextPartLength;

	/**
	 * Server name.
	 */
	private String serverName;

	/**
	 * {@link Flow} index to handle processing {@link HttpRequest}.
	 */
	private int requestHandlingFlowIndex;

	/*
	 * =================== CommunicationProtocolSource ======================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		// All properties are optional
	}

	@Override
	public CommunicationProtocol createCommunicationProtocol(MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = configurationContext.getManagedObjectSourceContext();

		// Obtain properties
		this.isSendStackTraceOnFailure = Boolean.parseBoolean(mosContext.getProperty(
				PROPERTY_IS_SEND_STACK_TRACE_ON_FAILURE, String.valueOf(DEFAULT_VALUE_IS_SEND_STACK_TRACE_ON_FAILURE)));
		this.maximumHttpRequestHeaders = Integer.parseInt(mosContext.getProperty(PROPERTY_MAXIMUM_HTTP_REQUEST_HEADERS,
				String.valueOf(DEFAULT_VALUE_MAXIMUM_HTTP_REQUEST_HEADERS)));
		this.maximumRequestBodyLength = Long.parseLong(mosContext.getProperty(PROPERTY_MAXIMUM_REQUEST_BODY_LENGTH,
				String.valueOf(DEFAULT_VALUE_MAXIMUM_REQUEST_BODY_LENGTH)));
		this.maxTextPartLength = Integer.parseInt(mosContext.getProperty(PROPERTY_MAXIMUM_TEXT_PART_LENGTH,
				String.valueOf(DEFAULT_VALUE_MAXIMUM_TEXT_PART_LENGTH)));

		// Obtain the server name
		InputStream serverNameInput = mosContext
				.getResource(HttpCommunicationProtocol.class.getPackage().getName().replace('.', '/') + "/Server.txt");
		Reader reader = new InputStreamReader(serverNameInput);
		StringWriter serverName = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			serverName.write(character);
		}
		this.serverName = serverName.toString();

		// Specify types
		// configurationContext.setManagedObjectClass(HttpManagedObjectImpl.class);
		configurationContext.setObjectClass(ServerHttpConnection.class);

		// Provide the flow to handle the HTTP request
		this.requestHandlingFlowIndex = configurationContext.addFlow(ServerHttpConnection.class)
				.setLabel("HANDLE_HTTP_REQUEST").getIndex();

		// Ensure connection is cleaned up when process finished
		// mosContext.getRecycleFunction(new
		// HttpCleanupManagedFunction()).linkParameter(0,
		// RecycleManagedObjectParameter.class);

		// Return this as the server
		return this;
	}

	/*
	 * ====================== CommunicationProtocol ============================
	 */

	@Override
	public HttpConnectionHandler createConnectionHandler(Connection connection,
			ManagedObjectExecuteContext<Indexed> executeContext) {
		return new HttpConnectionHandler(false, null, null);
	}

}