/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.value.load.ValueLoader;
import net.officefloor.web.value.load.ValueLoaderFactory;
import net.officefloor.web.value.load.ValueLoaderSource;

/**
 * {@link ManagedObjectSource} to cache creation of an {@link Object} within the
 * {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpRequestObjectManagedObjectSource
		extends AbstractManagedObjectSource<HttpRequestObjectManagedObjectSource.HttpRequestObjectDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum HttpRequestObjectDependencies {
		HTTP_REQUEST_STATE
	}

	/**
	 * Name of property containing the class name.
	 */
	public static final String PROPERTY_CLASS_NAME = "class.name";

	/**
	 * Name of property containing the name to bind the object within the
	 * {@link HttpRequestState}.
	 */
	public static final String PROPERTY_BIND_NAME = "bind.name";

	/**
	 * Name of property flagging whether to load the {@link HttpRequestState} values
	 * to a new object.
	 */
	public static final String PROPERTY_IS_LOAD_HTTP_PARAMETERS = "load.http.parameters";

	/**
	 * Property to obtain whether the {@link ValueLoader} is case insensitive in
	 * matching parameter names.
	 */
	public static final String PROPERTY_CASE_INSENSITIVE = "http.parameters.case.insensitive";

	/**
	 * Property prefix for the aliases.
	 */
	public static final String PROPERTY_PREFIX_ALIAS = "http.parameters.alias.";

	/**
	 * Class of the object.
	 */
	private Class<?> objectClass;

	/**
	 * Name to bind the object within the {@link HttpRequestState}.
	 */
	private String bindName;

	/**
	 * {@link ValueLoaderFactory}.
	 */
	private ValueLoaderFactory<Serializable> valueLoaderFactory = null;

	/*
	 * ======================= ManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASS_NAME, "Class");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<HttpRequestObjectDependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(PROPERTY_CLASS_NAME);
		this.objectClass = mosContext.loadClass(className);

		// Object must be serializable
		if (!(Serializable.class.isAssignableFrom(this.objectClass))) {
			throw new Exception(HttpRequestState.class.getSimpleName() + " object " + this.objectClass.getName()
					+ " must be " + Serializable.class.getSimpleName());
		}

		// Obtain the overridden bind name
		this.bindName = mosContext.getProperty(PROPERTY_BIND_NAME, null);

		// Specify the meta-data
		context.setObjectClass(this.objectClass);
		context.setManagedObjectClass(HttpRequestObjectManagedObject.class);
		context.addDependency(HttpRequestObjectDependencies.HTTP_REQUEST_STATE, HttpRequestState.class);

		// Determine if load parameters
		boolean isLoadParameters = Boolean
				.parseBoolean(mosContext.getProperty(PROPERTY_IS_LOAD_HTTP_PARAMETERS, String.valueOf(false)));
		if (isLoadParameters) {

			// Obtain whether case insensitive (true by default)
			boolean isCaseInsensitive = Boolean
					.parseBoolean(mosContext.getProperty(PROPERTY_CASE_INSENSITIVE, Boolean.toString(true)));

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
			ValueLoaderSource loaderSource = new ValueLoaderSource(this.objectClass, isCaseInsensitive, aliasMappings,
					null);
			this.valueLoaderFactory = (ValueLoaderFactory<Serializable>) loaderSource
					.sourceValueLoaderFactory(this.objectClass);
		}
	}

	@Override
	protected ManagedObject getManagedObject() {
		return new HttpRequestObjectManagedObject();
	}

	/**
	 * {@link ManagedObject} to retrieve the object from the
	 * {@link HttpRequestState}.
	 */
	public class HttpRequestObjectManagedObject
			implements ContextAwareManagedObject, CoordinatingManagedObject<HttpRequestObjectDependencies> {

		/**
		 * Name to bind the object to the {@link HttpRequestState}.
		 */
		private String boundName;

		/**
		 * Object.
		 */
		private Serializable object;

		/*
		 * ====================== ManagedObject =============================
		 */

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			// Use bind name in preference to managed object name
			this.boundName = (HttpRequestObjectManagedObjectSource.this.bindName != null
					? HttpRequestObjectManagedObjectSource.this.bindName
					: context.getBoundName());
		}

		@Override
		public void loadObjects(ObjectRegistry<HttpRequestObjectDependencies> registry) throws Throwable {

			// Obtain the HTTP request state
			HttpRequestState state = (HttpRequestState) registry
					.getObject(HttpRequestObjectDependencies.HTTP_REQUEST_STATE);

			// Lazy obtain the object
			this.object = state.getAttribute(this.boundName);
			if (this.object == null) {
				// Instantiate and register the object
				this.object = (Serializable) HttpRequestObjectManagedObjectSource.this.objectClass
						.getDeclaredConstructor().newInstance();
				state.setAttribute(this.boundName, this.object);

				// Determine if load parameters
				if (HttpRequestObjectManagedObjectSource.this.valueLoaderFactory != null) {
					// Load parameters from the request
					ValueLoader valueLoader = HttpRequestObjectManagedObjectSource.this.valueLoaderFactory
							.createValueLoader(this.object);
					state.loadValues(valueLoader);
				}
			}
		}

		@Override
		public Object getObject() {
			return this.object;
		}
	}

}
