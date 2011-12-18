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

package net.officefloor.plugin.web.http.session.object.source;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.object.HttpSessionObject;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectManagedObject;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectManagedObjectSource;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectManagedObjectSource.HttpSessionObjectDependencies;

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
		ManagedObjectLoaderUtil
				.validateSpecification(HttpSessionObjectManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpSessionObject.class);
		type.addDependency(HttpSessionObjectDependencies.HTTP_SESSION.name(),
				HttpSession.class, null,
				HttpSessionObjectDependencies.HTTP_SESSION.ordinal(),
				HttpSessionObjectDependencies.HTTP_SESSION);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpSessionObjectManagedObjectSource.class);
	}

	/**
	 * Ensure can load the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testLoad() throws Throwable {

		final Object SESSION_OBJECT = "Session Object";

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
		httpSession.setAttribute(BOUND_NAME, SESSION_OBJECT);
		this.recordReturn(httpSession, httpSession.getAttribute(BOUND_NAME),
				SESSION_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
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
		assertTrue("Incorrect object type", object instanceof HttpSessionObject);

		// Ensure able to interact with HTTP session
		HttpSessionObject<Object> httpSessionObject = (HttpSessionObject<Object>) object;
		assertNull("Initially should not obtain object from session",
				httpSessionObject.getSessionObject());
		httpSessionObject.setSessionObject(SESSION_OBJECT);
		assertEquals("Incorrect loaded session object", SESSION_OBJECT,
				httpSessionObject.getSessionObject());

		this.verifyMockObjects();
	}

}