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
package net.officefloor.plugin.web.http.session.attribute;

import java.io.Serializable;

import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeManagedObjectSource.HttpSessionAttributeDependencies;

/**
 * {@link ManagedObject} for the {@link HttpSessionAttribute}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeManagedObject implements
		NameAwareManagedObject,
		CoordinatingManagedObject<HttpSessionAttributeDependencies>,
		HttpSessionAttribute<Serializable> {

	/**
	 * Bound name to register the Object with the {@link HttpSession}.
	 */
	private String boundName;

	/**
	 * {@link HttpSession}.
	 */
	private HttpSession httpSession;

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
			ObjectRegistry<HttpSessionAttributeDependencies> registry)
			throws Throwable {

		// Obtain the HTTP Session
		this.httpSession = (HttpSession) registry
				.getObject(HttpSessionAttributeDependencies.HTTP_SESSION);
	}

	/*
	 * ========================= ManagedObject ================================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ======================== HttpSessionAttribute ==========================
	 */

	@Override
	public Serializable getSessionObject() {
		// Return the object from the Session.
		// (always go to session in case may have changed by other means)
		return this.httpSession.getAttribute(this.boundName);
	}

	@Override
	public void setSessionObject(Serializable sessionObject) {
		// Load object into the session
		this.httpSession.setAttribute(this.boundName, sessionObject);
	}

}