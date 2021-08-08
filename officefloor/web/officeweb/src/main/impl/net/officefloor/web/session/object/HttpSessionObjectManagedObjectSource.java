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

package net.officefloor.web.session.object;

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.object.HttpSessionObjectManagedObject.Dependencies;

/**
 * {@link ManagedObjectSource} to cache creation of an {@link Object} within the
 * {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpSessionObjectManagedObjectSource extends AbstractManagedObjectSource<Dependencies, None> {

	/**
	 * Name of property containing the class name.
	 */
	public static final String PROPERTY_CLASS_NAME = "class.name";

	/**
	 * Name of property containing the name to bind the object within the
	 * {@link HttpSession}.
	 */
	public static final String PROPERTY_BIND_NAME = "bind.name";

	/**
	 * Class of the object.
	 */
	private Class<?> objectClass;

	/**
	 * Name to bind the object within the {@link HttpSession}.
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

		// Object must be serializable
		if (!(Serializable.class.isAssignableFrom(this.objectClass))) {
			throw new Exception(HttpSession.class.getSimpleName() + " object " + this.objectClass.getName()
					+ " must be " + Serializable.class.getSimpleName());
		}

		// Obtain the overridden bind name
		this.bindName = mosContext.getProperty(PROPERTY_BIND_NAME, null);

		// Specify the meta-data
		context.setObjectClass(this.objectClass);
		context.setManagedObjectClass(HttpSessionObjectManagedObject.class);
		context.addDependency(Dependencies.HTTP_SESSION, HttpSession.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSessionObjectManagedObject(this.objectClass, this.bindName);
	}

}
