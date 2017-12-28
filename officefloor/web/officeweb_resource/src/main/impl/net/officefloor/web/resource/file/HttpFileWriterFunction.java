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
package net.officefloor.web.resource.file;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.resource.AbstractHttpFile;
import net.officefloor.web.resource.HttpFile;

/**
 * {@link ManagedFunctionFactory} to write a {@link HttpFile} to the
 * {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWriterFunction
		extends StaticManagedFunction<HttpFileWriterFunction.HttpFileWriterFunctionDependencies, None> {

	/**
	 * Keys for the dependencies.
	 */
	public static enum HttpFileWriterFunctionDependencies {
		HTTP_FILE, SERVER_HTTP_CONNECTION
	}

	/*
	 * ===================== ManagedFunction =============================
	 */

	@Override
	public Object execute(ManagedFunctionContext<HttpFileWriterFunctionDependencies, None> context) throws IOException {

		// Obtain the dependencies
		HttpFile httpFile = (HttpFile) context.getObject(HttpFileWriterFunctionDependencies.HTTP_FILE);
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpFileWriterFunctionDependencies.SERVER_HTTP_CONNECTION);

		// Write the file
		AbstractHttpFile.writeHttpFile(httpFile, connection.getResponse());

		// Return nothing as file written to response
		return null;
	}

}