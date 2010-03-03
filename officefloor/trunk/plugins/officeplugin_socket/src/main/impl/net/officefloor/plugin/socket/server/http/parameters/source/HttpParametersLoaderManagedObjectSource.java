/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http.parameters.source;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parameters.HttpParametersLoader;
import net.officefloor.plugin.socket.server.http.parameters.HttpParametersLoaderImpl;

/**
 * {@link ManagedObjectSource} to load the {@link HttpRequest} parameters onto a
 * dependency Object.
 *
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderManagedObjectSource extends
		AbstractManagedObjectSource<HttpParametersLoaderDependencies, None> {

	/**
	 * Property to obtain the fully qualified type name of the Object to have
	 * parameters loaded on it.
	 */
	public static final String PROPERTY_TYPE_NAME = "type.name";

	/**
	 * Property to obtain whether the {@link HttpParametersLoader} is case
	 * sensitive in matching parameter names.
	 */
	public static final String PROPERTY_CASE_SENSITIVE = "case.sensitive";

	/**
	 * Property prefix for an alias.
	 */
	public static final String PROPERTY_PREFIX_ALIAS = "alias.";

	/**
	 * {@link HttpParametersLoader}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpParametersLoader loader = new HttpParametersLoaderImpl<Object>();

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TYPE_NAME);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(
			MetaDataContext<HttpParametersLoaderDependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the type
		String typeName = mosContext.getProperty(PROPERTY_TYPE_NAME);
		Class<?> type = mosContext.getClassLoader().loadClass(typeName);

		// Obtain whether case sensitive (true by default)
		boolean isCaseSensitive = Boolean.parseBoolean(mosContext.getProperty(
				PROPERTY_CASE_SENSITIVE, Boolean.toString(false)));

		// Create the alias mappings
		Map<String, String> aliasMappings = new HashMap<String, String>();
		for (String name : mosContext.getProperties().stringPropertyNames()) {

			// Determine if alias property
			if (!name.startsWith(PROPERTY_PREFIX_ALIAS)) {
				continue;
			}

			// Obtain the alias and corresponding parameter name
			String alias = name.substring(PROPERTY_PREFIX_ALIAS.length());
			String parameterName = mosContext.getProperty(name);

			// Add the alias mapping
			aliasMappings.put(alias, parameterName);
		}

		// Initialise the HTTP parameters loader
		this.loader.init(type, aliasMappings, isCaseSensitive);

		// Load the meta-data
		context.setManagedObjectClass(HttpParametersLoaderManagedObject.class);
		context.setObjectClass(type);
		context.addDependency(
				HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class);
		context.addDependency(HttpParametersLoaderDependencies.OBJECT, type);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpParametersLoaderManagedObject();
	}

	/**
	 * {@link ManagedObject} to load the parameters.
	 */
	private class HttpParametersLoaderManagedObject implements
			CoordinatingManagedObject<HttpParametersLoaderDependencies> {

		/**
		 * Object to be loaded with parameters.
		 */
		private Object object;

		/*
		 * ==================== CoordinatingManagedObject =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void loadObjects(
				ObjectRegistry<HttpParametersLoaderDependencies> registry)
				throws Throwable {

			// Obtain the dependencies
			ServerHttpConnection connection = (ServerHttpConnection) registry
					.getObject(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
			this.object = registry
					.getObject(HttpParametersLoaderDependencies.OBJECT);

			// Load the parameters onto the object
			HttpParametersLoaderManagedObjectSource.this.loader.loadParameters(
					connection.getHttpRequest(), this.object);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.object;
		}
	}

}