/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.resource.source;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;

/**
 * Abstract {@link ManagedFunction} to send {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSendHttpFileFunction<R>
		extends StaticManagedFunction<AbstractSendHttpFileFunction.Dependencies, AbstractSendHttpFileFunction.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_PATH, HTTP_RESOURCES, SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link Flow} keys.
	 */
	public static enum Flows {
		NOT_AVAILABLE
	}

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path. May be <code>null</code>.
	 */
	public AbstractSendHttpFileFunction(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Obtains the {@link HttpResource}.
	 * 
	 * @param resources
	 *            Resources.
	 * @param resourcePath
	 *            Path to the {@link HttpResource}.
	 * @return {@link HttpResource}.
	 * @throws IOException
	 *             If fails to obtain the {@link HttpResource}.
	 */
	protected abstract HttpResource getHttpResource(R resources, String resourcePath) throws IOException;

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(ManagedFunctionContext<Dependencies, Flows> context) throws Throwable {

		// Obtain the dependencies
		HttpPath path = (HttpPath) context.getObject(Dependencies.HTTP_PATH);
		R resources = (R) context.getObject(Dependencies.HTTP_RESOURCES);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);

		// Determine if matches context path
		String resourcePath = path.getPath();
		if (resourcePath.startsWith(this.contextPath)) {
			if (resourcePath.length() == this.contextPath.length()) {
				// Root of the context
				resourcePath = "/";

			} else if (resourcePath.charAt(this.contextPath.length()) == '/') {
				// Strip off context path
				resourcePath = resourcePath.substring(this.contextPath.length());
			}

		} else {
			// Must match context path
			context.doFlow(Flows.NOT_AVAILABLE, path, null);
			return null;
		}

		// Obtain the HTTP resource
		HttpResource resource = this.getHttpResource(resources, resourcePath);

		// Obtain the HTTP file
		HttpFile file = null;
		if ((resource != null) && (resource.isExist())) {
			if (resource instanceof HttpDirectory) {

				// Obtain the default file for the directory
				HttpDirectory directory = (HttpDirectory) resource;
				file = directory.getDefaultHttpFile();
			} else {

				// Obtain as file
				file = (HttpFile) resource;
			}
		}

		// Determine if have HTTP file
		if (file == null) {
			// Not available
			context.doFlow(Flows.NOT_AVAILABLE, path, null);
			return null;
		}

		// Send the file
		file.writeTo(connection.getResponse());

		// File sent
		return null;
	}

}