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

import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeManagedObjectSource.HttpSessionAttributeDependencies;

/**
 * Tests the {@link HttpSessionAttributeManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeManagedObjectTest extends OfficeFrameTestCase {

	/**
	 * Bound name of the {@link HttpSessionAttributeManagedObject}.
	 */
	private static final String BOUND_NAME = "BOUND_NAME";

	/**
	 * {@link HttpSessionAttributeManagedObject} to test.
	 */
	private final HttpSessionAttributeManagedObject mo = new HttpSessionAttributeManagedObject();

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<HttpSessionAttributeDependencies> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link HttpSession}.
	 */
	private final HttpSession httpSession = this.createMock(HttpSession.class);

	/**
	 * Ensure able to obtain the object from the {@link HttpSession}.
	 */
	@SuppressWarnings("unchecked")
	public void testObtainObjectFromHttpSession() throws Throwable {

		Object availableObject = "Available Object";

		// Record loading the managed object
		this.recordReturn(this.objectRegistry,
				this.objectRegistry.getObject(HttpSessionAttributeDependencies.HTTP_SESSION), this.httpSession);
		this.recordReturn(this.httpSession, this.httpSession.getAttribute(BOUND_NAME), availableObject);

		// Test
		this.replayMockObjects();
		this.mo.setBoundManagedObjectName(BOUND_NAME);
		this.mo.loadObjects(this.objectRegistry);
		HttpSessionAttribute<Serializable> sessionObject = (HttpSessionAttribute<Serializable>) this.mo.getObject();
		Object object = sessionObject.getSessionObject();

		this.verifyMockObjects();

		// Ensure the correct object
		assertSame("Incorrect object", availableObject, object);
	}

	/**
	 * Ensure able to load an object into the {@link HttpSession}.
	 */
	@SuppressWarnings("unchecked")
	public void testLoadObjectIntoHttpSession() throws Throwable {

		final Serializable SESSION_OBJECT = "Session Object";

		// Record loading the managed object
		this.recordReturn(this.objectRegistry,
				this.objectRegistry.getObject(HttpSessionAttributeDependencies.HTTP_SESSION), this.httpSession);
		this.recordReturn(this.httpSession, this.httpSession.getAttribute(BOUND_NAME), null);
		this.httpSession.setAttribute(BOUND_NAME, SESSION_OBJECT);
		this.recordReturn(this.httpSession, this.httpSession.getAttribute(BOUND_NAME), SESSION_OBJECT);

		// Test
		this.replayMockObjects();
		this.mo.setBoundManagedObjectName(BOUND_NAME);
		this.mo.loadObjects(this.objectRegistry);
		HttpSessionAttribute<Serializable> sessionObject = (HttpSessionAttribute<Serializable>) this.mo.getObject();

		// Ensure not set in Session
		assertNull("No object should be in session", sessionObject.getSessionObject());

		// Ensure can load object into Session
		sessionObject.setSessionObject(SESSION_OBJECT);
		assertEquals("Incorrect loaded session object", SESSION_OBJECT, sessionObject.getSessionObject());

		this.verifyMockObjects();
	}

}