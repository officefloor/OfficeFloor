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

import java.io.FileNotFoundException;
import java.io.IOException;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.plugin.web.http.resource.AbstractHttpFile;
import net.officefloor.plugin.web.http.resource.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;

/**
 * {@link ManagedFunctionSource} for always sending a particular
 * {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Dependency keys for the {@link HttpFileFactoryFunction}.
	 */
	public static enum DependencyKeys {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Property to obtain the path to the resource on the class path.
	 */
	public static final String PROPERTY_RESOURCE_PATH = "resource.path";

	/**
	 * {@link ManagedFunction} name for writing the {@link HttpFile}.
	 */
	public static final String FUNCTION_HTTP_FILE = "file";

	/*
	 * ========================== WorkSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_RESOURCE_PATH, "Resource Path");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the properties
		String resourcePath = context.getProperty(PROPERTY_RESOURCE_PATH);

		// Create the class path HTTP resource factory
		HttpResourceFactory httpResourceFactory = SourceHttpResourceFactory.createHttpResourceFactory(context);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpResourceFactory.addHttpFileDescriber(describer);

		// Ensure resource path canonical
		if (!(resourcePath.startsWith("/"))) {
			resourcePath = "/" + resourcePath;
		}
		resourcePath = HttpApplicationLocationMangedObject.transformToCanonicalPath(resourcePath);

		// Obtain the HTTP file
		HttpResource resource = httpResourceFactory.createHttpResource(resourcePath);
		if (!(resource instanceof HttpFile)) {
			throw new FileNotFoundException("Can not find resource '" + resourcePath + "'");
		}
		HttpFile file = (HttpFile) resource;

		// Create the factory for the function
		SendHttpFileFunction factory = new SendHttpFileFunction(file);

		// Register the function
		ManagedFunctionTypeBuilder<DependencyKeys, None> function = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_HTTP_FILE, factory, DependencyKeys.class, None.class);
		function.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		function.addEscalation(IOException.class);
	}

	/**
	 * {@link ManagedFunction} to send the {@link HttpFile}.
	 */
	public static class SendHttpFileFunction extends StaticManagedFunction<DependencyKeys, None> {

		/**
		 * {@link HttpFile}.
		 */
		private final HttpFile file;

		/**
		 * Initiate.
		 * 
		 * @param file
		 *            {@link HttpFile}.
		 */
		public SendHttpFileFunction(HttpFile file) {
			this.file = file;
		}

		/*
		 * ========================= ManagedFunction =========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<DependencyKeys, None> context) throws IOException {

			// Obtain the response
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
			HttpResponse response = connection.getHttpResponse();

			// Write the HTTP file
			AbstractHttpFile.writeHttpFile(this.file, response);

			// Nothing to return
			return null;
		}
	}

}