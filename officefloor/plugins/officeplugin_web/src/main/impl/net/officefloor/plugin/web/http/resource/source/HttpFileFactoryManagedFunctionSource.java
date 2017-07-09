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

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceCreationListener;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;

/**
 * {@link ManagedFunctionSource} to locate a {@link HttpFile} on the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Enum of flows for the {@link HttpFileFactoryFunction}.
	 */
	public static enum HttpFileFactoryTaskFlows {
		HTTP_FILE_NOT_FOUND
	}

	/*
	 * ==================== AbstractWorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties as has defaults
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Create the class path HTTP file factory
		HttpResourceFactory httpFileFactory = SourceHttpResourceFactory.createHttpResourceFactory(context);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpFileFactory.addHttpFileDescriber(describer);

		// Create the handler for file not found
		HttpResourceCreationListener<HttpFileFactoryTaskFlows> fileNotFoundHandler = new HttpResourceCreationListener<HttpFileFactoryTaskFlows>() {
			@Override
			public void httpResourceCreated(HttpResource httpResource, ServerHttpConnection connection,
					ManagedFunctionContext<?, HttpFileFactoryTaskFlows> context) throws IOException {
				// Determine if the file exists
				if (!httpResource.isExist()) {
					// Invoke flow indicating file not found
					context.doFlow(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND, null, null);
				}
			}
		};

		// Create the HTTP file factory function
		HttpFileFactoryFunction<HttpFileFactoryTaskFlows> function = new HttpFileFactoryFunction<HttpFileFactoryTaskFlows>(
				httpFileFactory, fileNotFoundHandler);

		// Load the task to create the HTTP file
		ManagedFunctionTypeBuilder<DependencyKeys, HttpFileFactoryTaskFlows> functionTypeBuilder = namespaceTypeBuilder
				.addManagedFunctionType("FindFile", function, DependencyKeys.class, HttpFileFactoryTaskFlows.class);
		functionTypeBuilder.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		functionTypeBuilder.addObject(HttpApplicationLocation.class).setKey(DependencyKeys.HTTP_APPLICATION_LOCATION);
		ManagedFunctionFlowTypeBuilder<HttpFileFactoryTaskFlows> flowTypeBuilder = functionTypeBuilder.addFlow();
		flowTypeBuilder.setKey(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND);
		flowTypeBuilder.setArgumentType(HttpFile.class);
		functionTypeBuilder.setReturnType(HttpFile.class);
		functionTypeBuilder.addEscalation(IOException.class);
		functionTypeBuilder.addEscalation(InvalidHttpRequestUriException.class);
	}

}