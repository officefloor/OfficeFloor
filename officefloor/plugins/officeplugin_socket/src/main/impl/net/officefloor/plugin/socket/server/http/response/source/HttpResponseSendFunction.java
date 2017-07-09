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
package net.officefloor.plugin.socket.server.http.response.source;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunction} to trigger sending the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSendFunction
		extends StaticManagedFunction<HttpResponseSendFunction.HttpResponseSendTaskDependencies, None> {

	/**
	 * Keys for the dependencies.
	 */
	public static enum HttpResponseSendTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Adds the {@link ManagedFunctionType} information for
	 * {@link HttpResponseSendFunction}.
	 * 
	 * @param functionName
	 *            {@link ManagedFunction} name.
	 * @param functionFactory
	 *            {@link HttpResponseSendFunction}.
	 * @param functionTypeBuilder
	 *            {@link FunctionNamespaceBuilder}.
	 * @return {@link ManagedFunctionTypeBuilder} that added this
	 *         {@link HttpResponseSendFunction} type information.
	 */
	public static ManagedFunctionTypeBuilder<HttpResponseSendTaskDependencies, None> addFunctionType(
			String functionName, HttpResponseSendFunction functionFactory,
			FunctionNamespaceBuilder functionTypeBuilder) {
		ManagedFunctionTypeBuilder<HttpResponseSendTaskDependencies, None> function = functionTypeBuilder
				.addManagedFunctionType(functionName, functionFactory, HttpResponseSendTaskDependencies.class,
						None.class);
		function.addObject(ServerHttpConnection.class).setKey(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		function.addEscalation(IOException.class);
		return function;
	}

	/**
	 * HTTP status for the {@link HttpResponse}.
	 */
	private final int status;

	/**
	 * Content for the {@link HttpResponse}. May be <code>null</code>.
	 */
	private final ByteBuffer content;

	/**
	 * Initiate.
	 * 
	 * @param status
	 *            HTTP status for the {@link HttpResponse}.
	 * @param content
	 *            Content for {@link HttpResponse}. May be <code>null</code>.
	 */
	public HttpResponseSendFunction(int status, byte[] content) {
		this.status = status;
		if (content == null) {
			// No content
			this.content = null;
		} else {
			// Provide direct buffer with content
			ByteBuffer buffer = ByteBuffer.allocateDirect(content.length);
			buffer.put(content);
			buffer.flip();
			this.content = buffer.asReadOnlyBuffer();
		}
	}

	/*
	 * ====================== ManagedFunction ======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<HttpResponseSendTaskDependencies, None> context) throws IOException {

		// Obtain the response
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpResponse response = connection.getHttpResponse();

		// Provide the status if specified
		if (this.status > 0) {
			response.setStatus(this.status);
		}

		// Provide entity if have content
		if (this.content != null) {
			response.getEntity().write(this.content);
		}

		// Trigger sending the response
		response.send();

		// Return nothing as response sent
		return null;
	}

}