/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.autowire.AutoWireObject;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoader;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoaderImpl;

/**
 * {@link ManagedObjectSource} to instantiate an object and load
 * {@link HttpRequest} parameters into it.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersObjectManagedObjectSource
		extends
		AbstractManagedObjectSource<HttpParametersObjectManagedObjectSource.Dependencies, None> {

	/**
	 * Convenience method to auto-wire the
	 * {@link HttpParametersObjectManagedObjectSource}.
	 * 
	 * @param source
	 *            {@link AutoWireOfficeFloorSource}.
	 * @param objectClass
	 *            Object class.
	 * @return {@link AutoWireObject}.
	 */
	public static AutoWireObject autoWire(AutoWireOfficeFloorSource source,
			Class<?> objectClass) {
		AutoWireObject object = source.addManagedObject(
				HttpParametersObjectManagedObjectSource.class, null,
				objectClass);
		object.addProperty(
				HttpParametersObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				objectClass.getName());
		return object;
	}

	/**
	 * Dependencies for the {@link HttpParametersObjectManagedObjectSource}.
	 */
	public enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Property to obtain the fully qualified type name of the Object to be
	 * instantiated.
	 */
	public static final String PROPERTY_CLASS_NAME = "class.name";

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

	/**
	 * Class of the object.
	 */
	private Class<?> objectClass;

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASS_NAME, "Class");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(PROPERTY_CLASS_NAME);
		this.objectClass = mosContext.getClassLoader().loadClass(className);

		// Obtain whether case insensitive (true by default)
		boolean isCaseInsensitive = Boolean
				.parseBoolean(mosContext.getProperty(PROPERTY_CASE_INSENSITIVE,
						Boolean.toString(true)));

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
		this.loader.init(this.objectClass, aliasMappings, isCaseInsensitive,
				null);

		// Load the meta-data
		context.setManagedObjectClass(HttpParametersObjectManagedObject.class);
		context.setObjectClass(this.objectClass);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpParametersObjectManagedObject();
	}

	/**
	 * {@link ManagedObject} to instantiate the object and load the parameters.
	 */
	private class HttpParametersObjectManagedObject implements
			CoordinatingManagedObject<Dependencies> {

		/**
		 * Object to be loaded with parameters.
		 */
		private Object object;

		/*
		 * ==================== CoordinatingManagedObject =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {

			// Instantiate the object
			this.object = HttpParametersObjectManagedObjectSource.this.objectClass
					.newInstance();

			// Obtain the dependencies
			ServerHttpConnection connection = (ServerHttpConnection) registry
					.getObject(Dependencies.SERVER_HTTP_CONNECTION);

			// Load the parameters onto the object
			HttpParametersObjectManagedObjectSource.this.loader.loadParameters(
					connection.getHttpRequest(), this.object);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.object;
		}
	}

}