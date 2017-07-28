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
package net.officefloor.plugin.web.http.parameters.source;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.parameters.HttpParametersException;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoader;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoaderImpl;

/**
 * {@link ManagedFunctionSource} to load the {@link HttpRequest} parameters onto
 * a dependency Object.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Property to obtain the fully qualified type name of the Object to have
	 * parameters loaded on it.
	 */
	public static final String PROPERTY_TYPE_NAME = "type.name";

	/**
	 * Property to obtain whether the {@link HttpParametersLoader} is case
	 * insensitive in matching parameter names.
	 */
	public static final String PROPERTY_CASE_INSENSITIVE = "case.insensitive";

	/**
	 * Property prefix for an alias.
	 */
	public static final String PROPERTY_PREFIX_ALIAS = "alias.";

	/**
	 * {@link HttpParametersLoader}.
	 */
	@SuppressWarnings("rawtypes")
	private final HttpParametersLoader loader = new HttpParametersLoaderImpl<Object>();

	/*
	 * ===================== AbstractWorkSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TYPE_NAME);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the type
		String typeName = context.getProperty(PROPERTY_TYPE_NAME);
		Class<?> type = context.loadClass(typeName);

		// Obtain whether case insensitive (true by default)
		boolean isCaseInsensitive = Boolean
				.parseBoolean(context.getProperty(PROPERTY_CASE_INSENSITIVE, Boolean.toString(true)));

		// Create the alias mappings
		Map<String, String> aliasMappings = new HashMap<String, String>();
		for (String name : context.getPropertyNames()) {

			// Determine if alias property
			if (!name.startsWith(PROPERTY_PREFIX_ALIAS)) {
				continue;
			}

			// Obtain the alias and corresponding parameter name
			String alias = name.substring(PROPERTY_PREFIX_ALIAS.length());
			String parameterName = context.getProperty(name);

			// Add the alias mapping
			aliasMappings.put(alias, parameterName);
		}

		// Initialise the loader
		this.loader.init(type, aliasMappings, isCaseInsensitive, null);

		// Build the function
		ManagedFunctionTypeBuilder<HttpParametersLoaderDependencies, None> functionBuilder = namespaceTypeBuilder
				.addManagedFunctionType("LOADER", new HttpParametersLoaderFunction(),
						HttpParametersLoaderDependencies.class, None.class);
		functionBuilder.addObject(ServerHttpConnection.class)
				.setKey(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
		functionBuilder.addObject(type).setKey(HttpParametersLoaderDependencies.OBJECT);
		functionBuilder.setReturnType(type);
		functionBuilder.addEscalation(IOException.class);
		functionBuilder.addEscalation(HttpParametersException.class);
	}

	/**
	 * {@link ManagedFunction} to load the {@link HttpRequest} parameters onto a
	 * dependency Object.
	 */
	public class HttpParametersLoaderFunction extends StaticManagedFunction<HttpParametersLoaderDependencies, None> {

		/*
		 * ======================== ManagedFunction ==========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public Object execute(ManagedFunctionContext<HttpParametersLoaderDependencies, None> context)
				throws IOException, HttpParametersException {

			// Obtain the dependencies
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
			Object object = context.getObject(HttpParametersLoaderDependencies.OBJECT);

			// Load the parameters onto the object
			HttpParametersLoaderManagedFunctionSource.this.loader.loadParameters(connection.getHttpRequest(), object);

			// Return the object
			return object;
		}
	}

}