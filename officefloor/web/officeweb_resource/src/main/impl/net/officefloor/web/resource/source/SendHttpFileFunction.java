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
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from the
 * {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class SendHttpFileFunction
		extends StaticManagedFunction<SendHttpFileFunction.Dependencies, SendHttpFileFunction.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_PATH, HTTP_RESOURCE_STORE, SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link Flow} keys.
	 */
	public static enum Flows {
		NOT_AVAILABLE
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Dependencies, Flows> context) throws Throwable {

		// Obtain the dependencies
		HttpPath path = (HttpPath) context.getObject(Dependencies.HTTP_PATH);
		HttpResourceStore store = (HttpResourceStore) context.getObject(Dependencies.HTTP_RESOURCE_STORE);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);

		// Obtain the HTTP resource
		HttpResource resource = store.getHttpResource(path.getPath());

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
			context.doFlow(Flows.NOT_AVAILABLE, path, null);
			return null;
		}

		// Send the file
		file.writeTo(connection.getResponse());

		// File sent
		return null;
	}

}