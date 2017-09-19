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
package net.officefloor.plugin.web.http.session.object;

import java.io.Serializable;

import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link ManagedObject} to cache creation of an {@link Object} within the
 * {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObject
		implements NameAwareManagedObject, CoordinatingManagedObject<HttpSessionObjectManagedObject.Dependencies> {

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
	 * @param objectClass
	 *            Class of the object.
	 * @param bindName
	 *            Specific name to bind the object into the {@link HttpSession}.
	 */
	public HttpSessionObjectManagedObject(Class<?> objectClass, String bindName) {
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

		// Obtain the HTTP session
		HttpSession httpSession = (HttpSession) registry.getObject(Dependencies.HTTP_SESSION);

		// Lazy obtain the object
		this.object = httpSession.getAttribute(this.boundName);
		if (this.object == null) {
			this.object = (Serializable) this.objectClass.newInstance();
			httpSession.setAttribute(this.boundName, this.object);
		}
	}

	@Override
	public Object getObject() throws Throwable {
		return this.object;
	}

}