/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.plugin.socket.server.CommunicationProtocol;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.HttpConversation;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpConversationImpl;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpManagedObjectImpl;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * HTTP {@link CommunicationProtocol}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCommunicationProtocol implements
		CommunicationProtocol<HttpConnectionHandler>,
		ServerSocketHandler<HttpConnectionHandler> {

	/**
	 * Maximum number of {@link HttpHeader} instances per {@link HttpRequest}.
	 */
	// TODO provide via property
	private int maximumHttpRequestHeaders = 255;

	/**
	 * Maximum length in bytes of the {@link HttpRequest} body.
	 */
	// TODO provide via property
	private long maximumRequestBodyLength = (1024 * 1024);

	/**
	 * Timeout of the {@link Connection} in milliseconds.
	 */
	// TODO provide via property
	private long connectionTimeout = 5 * 60 * 1000;

	/**
	 * Maximum length of text part for {@link HttpRequest}.
	 */
	// TODO provide via property
	private int maxTextPartLength = 255;

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private BufferSquirtFactory bufferSquirtFactory;

	/**
	 * {@link Flow} index to handle processing {@link HttpRequest}.
	 */
	private int requestHandlingFlowIndex;

	/*
	 * ====================== CommunicationProtocol =========================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		// TODO add the fields as specifications
	}

	@Override
	public ServerSocketHandler<HttpConnectionHandler> createServerSocketHandler(
			MetaDataContext<None, Indexed> context,
			BufferSquirtFactory bufferSquirtFactory) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context
				.getManagedObjectSourceContext();
		this.bufferSquirtFactory = bufferSquirtFactory;

		// Specify types
		context.setManagedObjectClass(HttpManagedObjectImpl.class);
		context.setObjectClass(ServerHttpConnection.class);

		// Provide the flow to handle the HTTP request
		this.requestHandlingFlowIndex = context.addFlow(
				ServerHttpConnection.class).setLabel("HANDLE_HTTP_REQUEST")
				.getIndex();

		// Ensure connection is cleaned up when process finished
		new CleanupTask().registerAsRecycleTask(mosContext, "cleanup");

		// Return this as the server socket handler
		return this;
	}

	/*
	 * ====================== ServerSocketHandler ============================
	 */

	@Override
	public Server<HttpConnectionHandler> createServer() {
		return new HttpServer(this.requestHandlingFlowIndex);
	}

	@Override
	public HttpConnectionHandler createConnectionHandler(Connection connection) {
		HttpConversation conversation = new HttpConversationImpl(connection,
				this.bufferSquirtFactory);
		HttpRequestParser parser = new HttpRequestParserImpl(
				this.maximumHttpRequestHeaders, this.maximumRequestBodyLength);
		return new HttpConnectionHandler(conversation, parser,
				this.maxTextPartLength, this.connectionTimeout);
	}

}