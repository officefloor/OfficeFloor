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

import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.web.session.HttpSession;

/**
 * {@link ManagedObject} to cache creation of an {@link Object} within the
 * {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObject
		implements ContextAwareManagedObject, CoordinatingManagedObject<HttpSessionObjectManagedObject.Dependencies> {

	/**
	 * Dependency keys for the {@link HttpSessionObjectManagedObject}.
	 */
	public static enum Dependencies {
		HTTP_SESSION
	}

	/**
	 * Class of the object.
	 */
	private final Class<?> objectClass;

	/**
	 * Specific name to bind the object into the {@link HttpSession}.
	 */
	private final String bindName;

	/**
	 * Name to bind the object to the {@link HttpSession}.
	 */
	private String boundName;

	/**
	 * Object.
	 */
	private Serializable object;

	/**
	 * Initiate.
	 * 
	 * @param objectClass Class of the object.
	 * @param bindName    Specific name to bind the object into the
	 *                    {@link HttpSession}.
	 */
	public HttpSessionObjectManagedObject(Class<?> objectClass, String bindName) {
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

		// Obtain the HTTP session
		HttpSession httpSession = (HttpSession) registry.getObject(Dependencies.HTTP_SESSION);

		// Lazy obtain the object
		this.object = httpSession.getAttribute(this.boundName);
		if (this.object == null) {
			this.object = (Serializable) this.objectClass.getDeclaredConstructor().newInstance();
			httpSession.setAttribute(this.boundName, this.object);
		}
	}

	@Override
	public Object getObject() throws Throwable {
		return this.object;
	}

}
