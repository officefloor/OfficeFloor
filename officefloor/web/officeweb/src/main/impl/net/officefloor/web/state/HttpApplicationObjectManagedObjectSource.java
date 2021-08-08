/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.state;

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

/**
 * {@link ManagedObjectSource} to cache creation of an {@link Object} within the
 * {@link HttpApplicationState}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
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
			implements ContextAwareManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * Class of the object.
		 */
		private final Class<?> objectClass;

		/**
		 * Specific name to bind the object into the {@link HttpApplicationState}.
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
		 * @param objectClass Class of the object.
		 * @param bindName    Specific name to bind the object into the
		 *                    {@link HttpApplicationState}.
		 */
		public HttpApplicationObjectManagedObject(Class<?> objectClass, String bindName) {
			this.objectClass = objectClass;
			this.bindName = bindName;
		}

		/*
		 * ====================== ManagedObject =============================
		 */

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			// Use bind name in preference to managed object name
			this.boundName = (this.bindName != null ? this.bindName : context.getBoundName());
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the HTTP application state
			HttpApplicationState state = (HttpApplicationState) registry.getObject(Dependencies.HTTP_APPLICATION_STATE);

			// Lazy obtain the object
			this.object = state.getAttribute(this.boundName);
			if (this.object == null) {
				this.object = this.objectClass.getDeclaredConstructor().newInstance();
				state.setAttribute(this.boundName, this.object);
			}
		}

		@Override
		public Object getObject() throws Throwable {
			return this.object;
		}
	}

}
