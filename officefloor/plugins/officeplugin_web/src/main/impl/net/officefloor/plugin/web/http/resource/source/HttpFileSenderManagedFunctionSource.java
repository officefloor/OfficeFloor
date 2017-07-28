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
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.AbstractHttpFile;
import net.officefloor.plugin.web.http.resource.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceCreationListener;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.classpath.ClasspathHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;

/**
 * <p>
 * {@link ManagedFunctionSource} to send a {@link HttpFile}.
 * <p>
 * This provides common handling of sending a {@link HttpFile} by:
 * <ol>
 * <li>Creating the {@link HttpFile}</li>
 * <li>Sending the {@link HttpFile}</li>
 * <li>Sending a &quot;not found&quot; response if the {@link HttpFile} is not
 * found</li>
 * <li>Propagating failures in sending the {@link HttpFile}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSenderManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Property to obtain the path to the file containing the &quot;not
	 * found&quot; response.
	 */
	public static final String PROPERTY_NOT_FOUND_FILE_PATH = "not.found.file.path";

	/**
	 * Name of the {@link ManagedFunction} to send the {@link HttpFile}.
	 */
	public static final String FUNCTION_NAME = "SendFile";

	/**
	 * Name of the default not found file.
	 */
	private static final String DEFAULT_NOT_FOUND_FILE_NAME = "DefaultFileNotFound.html";

	/**
	 * Prefix for the default not found file.
	 */
	private static final String DEFAULT_NOT_FOUND_PREFIX = HttpFileSenderManagedFunctionSource.class.getPackage()
			.getName().replace('.', '/');

	/**
	 * Path on the class path to the default not found file.
	 */
	public static final String DEFAULT_NOT_FOUND_FILE_PATH = DEFAULT_NOT_FOUND_PREFIX + "/"
			+ DEFAULT_NOT_FOUND_FILE_NAME;

	/*
	 * ============================ WorkSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required configuration as defaults available
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the properties
		String notFoundContentPath = context.getProperty(PROPERTY_NOT_FOUND_FILE_PATH, null);

		// Obtain the class loader
		ClassLoader classLoader = context.getClassLoader();

		// Create the HTTP resource factory
		HttpResourceFactory httpResourceFactory = SourceHttpResourceFactory.createHttpResourceFactory(context);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpResourceFactory.addHttpFileDescriber(describer);

		// Initiate to obtain the not found content
		HttpResourceFactory notFoundResourceFactory;
		if (notFoundContentPath == null) {
			// Use default file not found content
			notFoundContentPath = DEFAULT_NOT_FOUND_FILE_NAME;
			notFoundResourceFactory = ClasspathHttpResourceFactory.getHttpResourceFactory(DEFAULT_NOT_FOUND_PREFIX,
					classLoader, notFoundContentPath);
			notFoundResourceFactory.addHttpFileDescriber(describer);

		} else {
			// Use specified not found from resource path
			notFoundResourceFactory = httpResourceFactory;
		}

		// Ensure file not found content path is canonical path
		if (!notFoundContentPath.startsWith("/")) {
			notFoundContentPath = "/" + notFoundContentPath;
		}
		notFoundContentPath = HttpApplicationLocationMangedObject.transformToCanonicalPath(notFoundContentPath);

		// Obtain the file not found content
		HttpResource notFoundResource = notFoundResourceFactory.createHttpResource(notFoundContentPath);
		if ((!notFoundResource.isExist()) || (!(notFoundResource instanceof HttpFile))) {
			// Must have file not found content
			throw new FileNotFoundException("Can not obtain file not found content: " + notFoundContentPath);
		}
		final HttpFile fileNotFoundContent = (HttpFile) notFoundResource;

		// Create the HTTP file creation listener
		HttpResourceCreationListener<None> httpFileCreationListener = new HttpResourceCreationListener<None>() {
			@Override
			public void httpResourceCreated(HttpResource httpResource, ServerHttpConnection connection,
					ManagedFunctionContext<?, None> context) throws IOException {

				// Obtain the response to write
				HttpResponse response = connection.getHttpResponse();

				// Determine if HTTP file exists
				if ((httpResource.isExist()) && (httpResource instanceof HttpFile)) {
					HttpFile httpFile = (HttpFile) httpResource;

					// Send the HTTP File
					AbstractHttpFile.writeHttpFile(httpFile, response);

					// Specify found status
					response.setStatus(HttpStatus.SC_OK);

				} else {
					// File not found so write file not found content
					AbstractHttpFile.writeHttpFile(fileNotFoundContent, response);

					// Specify not found status
					response.setStatus(HttpStatus.SC_NOT_FOUND);
				}

				// Send the response
				response.send();
			}
		};

		// Create the HTTP file factory function
		HttpFileFactoryFunction<None> function = new HttpFileFactoryFunction<None>(httpResourceFactory,
				httpFileCreationListener);

		// Load the task to send the HTTP file
		ManagedFunctionTypeBuilder<DependencyKeys, None> functionTypeBuilder = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_NAME, function, DependencyKeys.class, None.class);
		functionTypeBuilder.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		functionTypeBuilder.addObject(HttpApplicationLocation.class).setKey(DependencyKeys.HTTP_APPLICATION_LOCATION);
		functionTypeBuilder.addEscalation(IOException.class);
		functionTypeBuilder.addEscalation(InvalidHttpRequestUriException.class);
	}

}