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
package net.officefloor.plugin.json.read;

import java.io.InputStream;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;

/**
 * {@link ManagedObjectSource} to read the JSON {@link HttpRequest} pay load
 * onto a Java Object graph. The top level Java Object is provided as the object
 * from the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonRequestReaderManagedObjectSource
		extends
		AbstractManagedObjectSource<JsonRequestReaderManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_REQUEST_STATE
	}

	/**
	 * {@link Property} name for the fully qualified {@link Class} name of the
	 * {@link Object} to load with JSON content.
	 */
	public static final String PROPERTY_JSON_OBJECT_CLASS = "json.object.class";

	/**
	 * Name of optional {@link Property} to specify the name the {@link Object}
	 * is bound to the {@link HttpRequestState}.
	 */
	public static final String PROPERTY_BIND_NAME = "bind.name";

	/**
	 * Object {@link Class}.
	 */
	private Class<? extends Serializable> objectClass;

	/**
	 * Name to bind the {@link Object} within the {@link HttpRequestState}.
	 */
	private String bindName;

	/**
	 * {@link ObjectMapper}.
	 */
	private ObjectMapper mapper;

	/*
	 * ========================= ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_JSON_OBJECT_CLASS, "Class");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the Object class
		String objectClassName = mosContext
				.getProperty(PROPERTY_JSON_OBJECT_CLASS);
		Class<?> objectClass = mosContext.loadClass(objectClassName);

		// Object must be serializable
		if (!(Serializable.class.isAssignableFrom(objectClass))) {
			throw new Exception("JSON object " + objectClass.getName()
					+ " must be " + Serializable.class.getSimpleName()
					+ " as stored in " + HttpRequestState.class.getSimpleName());
		}
		this.objectClass = (Class<? extends Serializable>) objectClass;

		// Obtain the overridden bind name
		this.bindName = mosContext.getProperty(PROPERTY_BIND_NAME, null);

		// Provide meta-data
		context.setObjectClass(this.objectClass);
		context.setManagedObjectClass(JsonRequestReaderManagedObject.class);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class);
		context.addDependency(Dependencies.HTTP_REQUEST_STATE,
				HttpRequestState.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context)
			throws Exception {
		// Create the object mapper
		this.mapper = new ObjectMapper();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JsonRequestReaderManagedObject();
	}

	/**
	 * {@link ManagedObject} for the JSON read Object.
	 */
	private class JsonRequestReaderManagedObject implements
			NameAwareManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * Name the Object is bound in the {@link HttpRequestState}.
		 */
		private String boundName;

		/**
		 * {@link ServerHttpConnection}.
		 */
		private ServerHttpConnection connection;

		/**
		 * {@link HttpRequestState}.
		 */
		private HttpRequestState requestState;

		/*
		 * ================== ManagedObject =============================
		 */

		@Override
		public void setBoundManagedObjectName(String boundManagedObjectName) {
			this.boundName = (JsonRequestReaderManagedObjectSource.this.bindName != null ? JsonRequestReaderManagedObjectSource.this.bindName
					: boundManagedObjectName);
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {

			// Obtain the dependencies
			this.connection = (ServerHttpConnection) registry
					.getObject(Dependencies.SERVER_HTTP_CONNECTION);
			this.requestState = (HttpRequestState) registry
					.getObject(Dependencies.HTTP_REQUEST_STATE);
		}

		@Override
		public Object getObject() throws Throwable {

			// Lazy obtain the object
			Serializable object = this.requestState
					.getAttribute(this.boundName);
			if (object == null) {

				// Obtain the JSON pay load
				HttpRequest request = this.connection.getHttpRequest();
				InputStream browseInputStream = request.getEntity()
						.createBrowseInputStream();
				if (browseInputStream.available() <= 0) {
					// No pay load, then provide empty object
					object = JsonRequestReaderManagedObjectSource.this.objectClass
							.newInstance();

				} else {
					// Load the object from the request
					object = JsonRequestReaderManagedObjectSource.this.mapper
							.readValue(
									browseInputStream,
									JsonRequestReaderManagedObjectSource.this.objectClass);
				}

				// Register the object
				this.requestState.setAttribute(this.boundName, object);
			}

			// Return the object
			return object;
		}
	}

}