/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import org.easymock.AbstractMatcher;

import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.object.source.HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies;

/**
 * Tests the {@link HttpSessionObjectManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectTest extends OfficeFrameTestCase {

	/**
	 * Bound name of the {@link HttpSessionObjectManagedObject}.
	 */
	private static final String BOUND_NAME = "BOUND_NAME";

	/**
	 * {@link HttpSessionObjectManagedObject} to test.
	 */
	private final HttpSessionObjectManagedObject mo = new HttpSessionObjectManagedObject(
			MockHttpSessionObject.class);

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<HttpSessionObjectDependencies> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link HttpSession}.
	 */
	private final HttpSession httpSession = this.createMock(HttpSession.class);

	/**
	 * Ensure able to obtain the object from the {@link HttpSession}.
	 */
	public void testObtainObjectFromHttpSession() throws Throwable {

		MockHttpSessionObject availableObject = new MockHttpSessionObject();

		// Record loading the managed object
		this.recordReturn(this.objectRegistry, this.objectRegistry
				.getObject(HttpSessionObjectDependencies.HTTP_SESSION),
				this.httpSession);
		this.recordReturn(this.httpSession, this.httpSession
				.getAttribute(BOUND_NAME), availableObject);

		// Test
		this.replayMockObjects();
		this.mo.setBoundManagedObjectName(BOUND_NAME);
		this.mo.loadObjects(this.objectRegistry);
		Object object = this.mo.getObject();
		this.verifyMockObjects();

		// Ensure the correct object
		assertSame("Incorrect object", availableObject, object);
	}

	/**
	 * Ensure able to instantiate and register object if not available in the
	 * {@link HttpSession}.
	 */
	public void testObjectNoAvailableInHttpSession() throws Throwable {

		// Record loading the managed object
		this.recordReturn(this.objectRegistry, this.objectRegistry
				.getObject(HttpSessionObjectDependencies.HTTP_SESSION),
				this.httpSession);
		this.recordReturn(this.httpSession, this.httpSession
				.getAttribute(BOUND_NAME), null);
		this.httpSession.setAttribute(BOUND_NAME, new MockHttpSessionObject());
		final MockHttpSessionObject[] registeredObject = new MockHttpSessionObject[1];
		this.control(this.httpSession).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect attribute name", BOUND_NAME, actual[0]);
				assertTrue("Must have object",
						(actual[1] instanceof MockHttpSessionObject));
				registeredObject[0] = (MockHttpSessionObject) actual[1];
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		this.mo.setBoundManagedObjectName(BOUND_NAME);
		this.mo.loadObjects(this.objectRegistry);
		Object object = this.mo.getObject();
		this.verifyMockObjects();

		// Ensure the correct object
		assertSame("Incorrect object", registeredObject[0], object);
	}

	/**
	 * Class to be instantiated from the {@link HttpSessionObjectManagedObject}.
	 */
	public static class MockHttpSessionObject {
	}

}