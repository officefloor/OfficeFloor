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
package net.officefloor.plugin.web.http.resource.source;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceCreationListener;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.NotExistHttpResource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.state.HttpApplicationState;

/**
 * {@link ManagedFunction} to locate a {@link HttpFile} via a
 * {@link HttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryFunction<F extends Enum<F>>
		extends StaticManagedFunction<HttpFileFactoryFunction.DependencyKeys, F> {

	/**
	 * Dependency keys for the {@link HttpFileFactoryFunction}.
	 */
	public enum DependencyKeys {
		SERVER_HTTP_CONNECTION, HTTP_APPLICATION_STATE
	}

	/**
	 * {@link HttpResourceFactory}.
	 */
	private final HttpResourceFactory httpResourceFactory;

	/**
	 * {@link HttpResourceCreationListener}.
	 */
	private final HttpResourceCreationListener<F> httpFileCreationListener;

	/**
	 * Initiate.
	 * 
	 * @param httpResourceFactory
	 *            {@link HttpResourceFactory}.
	 * @param httpFileCreationListener
	 *            {@link HttpResourceCreationListener}.
	 */
	public HttpFileFactoryFunction(HttpResourceFactory httpResourceFactory,
			HttpResourceCreationListener<F> httpFileCreationListener) {
		this.httpResourceFactory = httpResourceFactory;
		this.httpFileCreationListener = httpFileCreationListener;
	}

	/*
	 * =========================== ManagedFunction ===========================
	 */

	@Override
	public Object execute(ManagedFunctionContext<DependencyKeys, F> context) throws IOException {

		// Obtain the connection
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);

		// Obtain the resource
		HttpResource resource;
		try {

			// Obtain the file path
			HttpApplicationState applicationState = (HttpApplicationState) context
					.getObject(DependencyKeys.HTTP_APPLICATION_STATE);
			String filePath = applicationState.extractApplicationPath(connection);

			// Create the HTTP resource for the request
			resource = this.httpResourceFactory.createHttpResource(filePath);

			// Ensure obtain default file (if directory)
			if (resource instanceof HttpDirectory) {
				HttpDirectory directory = (HttpDirectory) resource;
				HttpFile defaultFile = directory.getDefaultFile();

				// Provide resource based on default file existing
				resource = (defaultFile != null ? defaultFile : new NotExistHttpResource(resource.getPath()));
			}

		} catch (HttpException ex) {
			String requestUriPath = connection.getRequest().getUri();
			resource = new NotExistHttpResource(requestUriPath);
		}

		// Notify the HTTP resource created
		this.httpFileCreationListener.httpResourceCreated(resource, connection, context);

		// Return the HTTP resource
		return resource;
	}

}