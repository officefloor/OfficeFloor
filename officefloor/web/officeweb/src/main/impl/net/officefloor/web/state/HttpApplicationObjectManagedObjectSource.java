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
package net.officefloor.web.state;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to cache creation of an {@link Object} within the
 * {@link HttpApplicationState}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationObjectManagedObjectSource
		extends AbstractManagedObjectSource<HttpApplicationObjectManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys for the {@link HttpApplicationObjectManagedObject}.
	 */
	public static enum Dependencies {
		HTTP_APPLICATION_STATE
	}

	/**
	 * Name of property containing the class name.
	 */
	public static final String PROPERTY_CLASS_NAME = "class.name";

	/**
	 * Name of property containing the name to bind the object within the
	 * {@link HttpApplicationState}.
	 */
	public static final String PROPERTY_BIND_NAME = "bind.name";

	/**
	 * Class of the object.
	 */
	private Class<?> objectClass;

	/**
	 * Name to bind the object within the {@link HttpApplicationState}.
	 */
	private String bindName;

	/*
	 * ======================= ManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASS_NAME, "Class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(PROPERTY_CLASS_NAME);
		this.objectClass = mosContext.loadClass(className);

		// Obtain the overridden bind name
		this.bindName = mosContext.getProperty(PROPERTY_BIND_NAME, null);

		// Specify the meta-data
		context.setObjectClass(this.objectClass);
		context.setManagedObjectClass(HttpApplicationObjectManagedObject.class);
		context.addDependency(Dependencies.HTTP_APPLICATION_STATE, HttpApplicationState.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpApplicationObjectManagedObject(this.objectClass, this.bindName);
	}

	/**
	 * {@link ManagedObject} to retrieve the object from the
	 * {@link HttpApplicationState}.
	 */
	public static class HttpApplicationObjectManagedObject
			implements NameAwareManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * Class of the object.
		 */
		private final Class<?> objectClass;

		/**
		 * Specific name to bind the object into the
		 * {@link HttpApplicationState}.
		 */
		private final String bindName;

		/**
		 * Name to bind the object to the {@link HttpApplicationState}.
		 */
		private String boundName;

		/**
		 * Object.
		 */
		private Object object;

		/**
		 * Initiate.
		 * 
		 * @param objectClass
		 *            Class of the object.
		 * @param bindName
		 *            Specific name to bind the object into the
		 *            {@link HttpApplicationState}.
		 */
		public HttpApplicationObjectManagedObject(Class<?> objectClass, String bindName) {
			this.objectClass = objectClass;
			this.bindName = bindName;
		}

		/*
		 * ====================== ManagedObject =============================
		 */

		@Override
		public void setBoundManagedObjectName(String boundManagedObjectName) {
			// Use bind name in preference to managed object name
			this.boundName = (this.bindName != null ? this.bindName : boundManagedObjectName);
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the HTTP application state
			HttpApplicationState state = (HttpApplicationState) registry.getObject(Dependencies.HTTP_APPLICATION_STATE);

			// Lazy obtain the object
			this.object = state.getAttribute(this.boundName);
			if (this.object == null) {
				this.object = this.objectClass.newInstance();
				state.setAttribute(this.boundName, this.object);
			}
		}

		@Override
		public Object getObject() throws Throwable {
			return this.object;
		}
	}

}