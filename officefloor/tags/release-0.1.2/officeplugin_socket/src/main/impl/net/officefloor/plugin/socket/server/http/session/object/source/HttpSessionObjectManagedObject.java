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

package net.officefloor.plugin.socket.server.http.session.object.source;

import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.object.source.HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies;

/**
 * {@link ManagedObject} for the {@link HttpSession} Object.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObject implements NameAwareManagedObject,
		CoordinatingManagedObject<HttpSessionObjectDependencies> {

	/**
	 * {@link Class} of the Object.
	 */
	private final Class<?> clazz;

	/**
	 * Bound name to register the Object with the {@link HttpSession}.
	 */
	private String boundName;

	/**
	 * {@link HttpSession}.
	 */
	private HttpSession httpSession;

	/**
	 * Object.
	 */
	private Object object;

	/**
	 * Initiate.
	 *
	 * @param clazz
	 *            {@link Class} of the Object.
	 */
	public HttpSessionObjectManagedObject(Class<?> clazz) {
		this.clazz = clazz;
	}

	/*
	 * ===================== NameAwareManagedObject ===========================
	 */

	@Override
	public void setBoundManagedObjectName(String boundManagedObjectName) {
		this.boundName = boundManagedObjectName;
	}

	/*
	 * ===================== CoordinatingManagedObject =========================
	 */

	@Override
	public void loadObjects(
			ObjectRegistry<HttpSessionObjectDependencies> registry)
			throws Throwable {

		// Obtain the HTTP Session
		this.httpSession = (HttpSession) registry
				.getObject(HttpSessionObjectDependencies.HTTP_SESSION);

		// Obtain the object
		this.object = this.httpSession.getAttribute(this.boundName);
		if (this.object == null) {
			// Not in HTTP Session, so create and register
			this.object = this.clazz.newInstance();
			this.httpSession.setAttribute(this.boundName, this.object);
		}
	}

	/*
	 * ========================= ManagedObject ================================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.object;
	}

}