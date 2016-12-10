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

import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeRetrieverManagedObjectSource.HttpSessionAttributeRetrieverDependencies;

/**
 * {@link ManagedObject} that retrieves the {@link Object} from the
 * {@link HttpSessionAttribute}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeRetrieverManagedObject
		implements
		CoordinatingManagedObject<HttpSessionAttributeRetrieverManagedObjectSource.HttpSessionAttributeRetrieverDependencies> {

	/**
	 * {@link HttpSessionAttribute}.
	 */
	private HttpSessionAttribute<?> httpSessionObject;

	/*
	 * ===================== CoordinatingManagedObject ===============
	 */

	@Override
	public void loadObjects(
			ObjectRegistry<HttpSessionAttributeRetrieverDependencies> registry)
			throws Throwable {

		// Obtain the HTTP session object
		this.httpSessionObject = (HttpSessionAttribute<?>) registry
				.getObject(HttpSessionAttributeRetrieverDependencies.HTTP_SESSION_OBJECT);
	}

	@Override
	public Object getObject() throws Throwable {
		// Always obtain from Session Object in case may change
		return this.httpSessionObject.getSessionObject();
	}

}