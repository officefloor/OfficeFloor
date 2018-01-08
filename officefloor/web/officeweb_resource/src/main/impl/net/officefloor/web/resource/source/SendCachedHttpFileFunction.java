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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from
 * {@link HttpResourceCache}.
 * 
 * @author Daniel Sagenschneider
 */
public class SendCachedHttpFileFunction
		extends StaticManagedFunction<SendCachedHttpFileFunction.Dependencies, SendCachedHttpFileFunction.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_RESOURCE_CACHE, SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link Flow} keys.
	 */
	public static enum Flows {
		NOT_CACHED
	}

	/**
	 * Path to the {@link HttpResource}.
	 */
	private final String path;

	/**
	 * Instantiate.
	 * 
	 * @param path
	 *            Path to the {@link HttpResource}.
	 */
	public SendCachedHttpFileFunction(String path) {
		this.path = path;
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Dependencies, Flows> context) throws Throwable {

		// Obtain the dependencies
		HttpResourceCache cache = (HttpResourceCache) context.getObject(Dependencies.HTTP_RESOURCE_CACHE);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);

		// Obtain the HTTP resource
		HttpResource resource = cache.getHttpResource(path);

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
			// Not cached
			context.doFlow(Flows.NOT_CACHED, null, null);
			return null;
		}

		// Send the file
		file.writeTo(connection.getResponse());

		// File sent
		return null;
	}

}