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

package net.officefloor.plugin.web.http.session.object.source;

import java.io.Serializable;

import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.object.HttpSessionObject;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectRetrieverManagedObjectSource.HttpSessionObjectRetrieverDependencies;

/**
 * Tests the {@link HttpSessionObjectRetrieverManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectRetrieverManagedObjectTest extends
		OfficeFrameTestCase {

	/**
	 * {@link HttpSessionObjectRetrieverManagedObject} to test.
	 */
	private final HttpSessionObjectRetrieverManagedObject mo = new HttpSessionObjectRetrieverManagedObject();

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<HttpSessionObjectRetrieverDependencies> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link HttpSessionObject}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpSessionObject<Serializable> sessionObject = this
			.createMock(HttpSessionObject.class);

	/**
	 * Ensure able to obtain the object from the {@link HttpSessionObject}.
	 */
	public void testRetrieveObjectFromHttpSessionObject() throws Throwable {

		final Object availableObject = "Available Object";

		// Record retrieving the managed object
		this.recordReturn(
				this.objectRegistry,
				this.objectRegistry
						.getObject(HttpSessionObjectRetrieverDependencies.HTTP_SESSION_OBJECT),
				this.sessionObject);
		this.recordReturn(this.sessionObject,
				this.sessionObject.getSessionObject(), availableObject);

		// Test
		this.replayMockObjects();
		this.mo.loadObjects(this.objectRegistry);
		Object object = this.mo.getObject();

		this.verifyMockObjects();

		// Ensure the correct object
		assertSame("Incorrect object", availableObject, object);
	}

	/**
	 * Ensure is <code>null</code> on no object available from the
	 * {@link HttpSession}.
	 */
	public void testNoObjectAvailableToRetrieve() throws Throwable {

		// Record retrieving the managed object
		this.recordReturn(
				this.objectRegistry,
				this.objectRegistry
						.getObject(HttpSessionObjectRetrieverDependencies.HTTP_SESSION_OBJECT),
				this.sessionObject);
		this.recordReturn(this.sessionObject,
				this.sessionObject.getSessionObject(), null);

		// Test
		this.replayMockObjects();
		this.mo.loadObjects(this.objectRegistry);
		Object object = this.mo.getObject();

		this.verifyMockObjects();

		// Ensure no object available
		assertNull("Should not retrieve object", object);
	}

}