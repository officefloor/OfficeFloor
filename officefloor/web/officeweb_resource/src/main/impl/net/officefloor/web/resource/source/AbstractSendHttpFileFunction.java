/*-
 * #%L
 * Web resources
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param contextPath Context path. May be <code>null</code>.
	 */
	public AbstractSendHttpFileFunction(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Obtains the {@link HttpResource}.
	 * 
	 * @param resources    Resources.
	 * @param resourcePath Path to the {@link HttpResource}.
	 * @return {@link HttpResource}.
	 * @throws IOException If fails to obtain the {@link HttpResource}.
	 */
	protected abstract HttpResource getHttpResource(R resources, String resourcePath) throws IOException;

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void execute(ManagedFunctionContext<Dependencies, Flows> context) throws Throwable {

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
			return;
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
			return;
		}

		// Send the file
		file.writeTo(connection.getResponse());
	}

}
