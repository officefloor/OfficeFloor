/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.application;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link HttpRequestHandlerMarkerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestHandlerMarkerManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(HttpRequestHandlerMarkerManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpRequestHandlerMarker.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpRequestHandlerMarkerManagedObjectSource.class);
	}

	/**
	 * Ensure can load.
	 */
	public void testLoad() throws Throwable {

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		HttpRequestHandlerMarkerManagedObjectSource source = loader
				.loadManagedObjectSource(HttpRequestHandlerMarkerManagedObjectSource.class);

		// Source the object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);
		Object object = managedObject.getObject();

		// Validate the object
		assertTrue("Incorrect type",
				(object instanceof HttpRequestHandlerMarker));
		assertSame("Should not be creating object as marker", source, object);
	}

}