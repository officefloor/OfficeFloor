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
package net.officefloor.web.resource.source;

import java.io.IOException;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.impl.FileExtensionHttpFileDescriber;
import net.officefloor.web.resource.impl.HttpResourceCreationListener;
import net.officefloor.web.resource.source.HttpFileFactoryFunction.DependencyKeys;
import net.officefloor.web.state.HttpApplicationState;

/**
 * {@link ManagedFunctionSource} to locate a {@link HttpFile} on the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Enum of flows for the {@link HttpFileFactoryFunction}.
	 */
	public static enum HttpFileFactoryFunctionFlows {
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
		HttpResourceStore httpFileFactory = SourceHttpResourceFactory.createHttpResourceFactory(context);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpFileFactory.addHttpFileDescriber(describer);

		// Create the handler for file not found
		HttpResourceCreationListener<HttpFileFactoryFunctionFlows> fileNotFoundHandler = new HttpResourceCreationListener<HttpFileFactoryFunctionFlows>() {
			@Override
			public void httpResourceCreated(HttpResource httpResource, ServerHttpConnection connection,
					ManagedFunctionContext<?, HttpFileFactoryFunctionFlows> context) throws IOException {
				// Determine if the file exists
				if (!httpResource.isExist()) {
					// Invoke flow indicating file not found
					context.doFlow(HttpFileFactoryFunctionFlows.HTTP_FILE_NOT_FOUND, null, null);
				}
			}
		};

		// Create the HTTP file factory function
		HttpFileFactoryFunction<HttpFileFactoryFunctionFlows> function = new HttpFileFactoryFunction<HttpFileFactoryFunctionFlows>(
				httpFileFactory, fileNotFoundHandler);

		// Load the task to create the HTTP file
		ManagedFunctionTypeBuilder<DependencyKeys, HttpFileFactoryFunctionFlows> functionTypeBuilder = namespaceTypeBuilder
				.addManagedFunctionType("FindFile", function, DependencyKeys.class, HttpFileFactoryFunctionFlows.class);
		functionTypeBuilder.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		functionTypeBuilder.addObject(HttpApplicationState.class).setKey(DependencyKeys.HTTP_APPLICATION_STATE);
		ManagedFunctionFlowTypeBuilder<HttpFileFactoryFunctionFlows> flowTypeBuilder = functionTypeBuilder.addFlow();
		flowTypeBuilder.setKey(HttpFileFactoryFunctionFlows.HTTP_FILE_NOT_FOUND);
		flowTypeBuilder.setArgumentType(HttpFile.class);
		functionTypeBuilder.setReturnType(HttpFile.class);
		functionTypeBuilder.addEscalation(IOException.class);
	}

}