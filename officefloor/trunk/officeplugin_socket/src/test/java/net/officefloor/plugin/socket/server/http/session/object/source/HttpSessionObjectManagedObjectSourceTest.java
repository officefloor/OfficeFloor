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

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.object.source.HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies;

/**
 * Tests the {@link HttpSessionObjectManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpSessionObjectManagedObjectSource.class,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockHttpSessionObject.class);
		type.addDependency(HttpSessionObjectDependencies.HTTP_SESSION.name(),
				HttpSession.class, HttpSessionObjectDependencies.HTTP_SESSION
						.ordinal(), HttpSessionObjectDependencies.HTTP_SESSION);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpSessionObjectManagedObjectSource.class,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockHttpSessionObject.class.getName());
	}

	/**
	 * Ensure can load the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testLoad() throws Throwable {

		final String BOUND_NAME = "BOUND";
		ObjectRegistry<HttpSessionObjectDependencies> objectRegistry = this
				.createMock(ObjectRegistry.class);
		HttpSession httpSession = this.createMock(HttpSession.class);

		// Record
		this.recordReturn(objectRegistry, objectRegistry
				.getObject(HttpSessionObjectDependencies.HTTP_SESSION),
				httpSession);
		this.recordReturn(httpSession, httpSession.getAttribute(BOUND_NAME),
				null);
		httpSession.setAttribute(BOUND_NAME, new MockHttpSessionObject());
		this.control(httpSession).setMatcher(
				new TypeMatcher(String.class, MockHttpSessionObject.class));

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockHttpSessionObject.class.getName());
		HttpSessionObjectManagedObjectSource mos = loader
				.loadManagedObjectSource(HttpSessionObjectManagedObjectSource.class);

		// Obtain the managed object (ensure correct)
		ManagedObject mo = mos.getManagedObject();
		assertTrue("Incorrect managed object type",
				(mo instanceof HttpSessionObjectManagedObject));

		// Obtain the object (to ensure correct class)
		HttpSessionObjectManagedObject httpSessionObjectMo = (HttpSessionObjectManagedObject) mo;
		httpSessionObjectMo.setBoundManagedObjectName(BOUND_NAME);
		httpSessionObjectMo.loadObjects(objectRegistry);
		Object object = mo.getObject();

		this.verifyMockObjects();

		// Ensure correct type of object
		assertEquals("Incorrect object type", MockHttpSessionObject.class,
				object.getClass());
	}

	/**
	 * Mock {@link HttpSession} object.
	 */
	public static class MockHttpSessionObject {
	}

}