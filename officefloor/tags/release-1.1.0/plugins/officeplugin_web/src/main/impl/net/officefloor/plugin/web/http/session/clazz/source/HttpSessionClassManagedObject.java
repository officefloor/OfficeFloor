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
package net.officefloor.plugin.web.http.session.clazz.source;

import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link ManagedObject} to cache creation of an {@link Object} within the
 * {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionClassManagedObject implements NameAwareManagedObject,
		CoordinatingManagedObject<HttpSessionClassManagedObject.Dependencies> {

	/**
	 * Dependency keys for the {@link HttpSessionClassManagedObject}.
	 */
	public static enum Dependencies {
		HTTP_SESSION
	}

	/**
	 * Class of the object.
	 */
	private final Class<?> objectClass;

	/**
	 * Name to bind the object to the {@link HttpSession}.
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
	 */
	public HttpSessionClassManagedObject(Class<?> objectClass) {
		this.objectClass = objectClass;
	}

	/*
	 * ====================== ManagedObject =============================
	 */

	@Override
	public void setBoundManagedObjectName(String boundManagedObjectName) {
		this.boundName = boundManagedObjectName;
	}

	@Override
	public void loadObjects(ObjectRegistry<Dependencies> registry)
			throws Throwable {

		// Obtain the HTTP session
		HttpSession httpSession = (HttpSession) registry
				.getObject(Dependencies.HTTP_SESSION);

		// Lazy obtain the object
		this.object = httpSession.getAttribute(this.boundName);
		if (this.object == null) {
			this.object = this.objectClass.newInstance();
			httpSession.setAttribute(this.boundName, this.object);
		}
	}

	@Override
	public Object getObject() throws Throwable {
		return this.object;
	}

}