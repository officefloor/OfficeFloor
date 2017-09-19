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

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.attribute.HttpSessionAttributeManagedObjectSource.HttpSessionAttributeDependencies;

/**
 * Tests the {@link HttpSessionAttributeManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionAttributeManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(HttpSessionAttributeManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpSessionAttribute.class);
		type.addDependency(HttpSessionAttributeDependencies.HTTP_SESSION.name(), HttpSession.class, null,
				HttpSessionAttributeDependencies.HTTP_SESSION.ordinal(), HttpSessionAttributeDependencies.HTTP_SESSION);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, HttpSessionAttributeManagedObjectSource.class);
	}

	/**
	 * Ensure can load the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testLoad() throws Throwable {

		final Serializable SESSION_OBJECT = "Session Object";

		final String BOUND_NAME = "BOUND";
		ObjectRegistry<HttpSessionAttributeDependencies> objectRegistry = this.createMock(ObjectRegistry.class);
		HttpSession httpSession = this.createMock(HttpSession.class);

		// Record
		this.recordReturn(objectRegistry, objectRegistry.getObject(HttpSessionAttributeDependencies.HTTP_SESSION),
				httpSession);
		this.recordReturn(httpSession, httpSession.getAttribute(BOUND_NAME), null);
		httpSession.setAttribute(BOUND_NAME, SESSION_OBJECT);
		this.recordReturn(httpSession, httpSession.getAttribute(BOUND_NAME), SESSION_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		HttpSessionAttributeManagedObjectSource mos = loader
				.loadManagedObjectSource(HttpSessionAttributeManagedObjectSource.class);

		// Obtain the managed object (ensure correct)
		ManagedObject mo = mos.getManagedObject();
		assertTrue("Incorrect managed object type", (mo instanceof HttpSessionAttributeManagedObject));

		// Obtain the object (to ensure correct class)
		HttpSessionAttributeManagedObject httpSessionObjectMo = (HttpSessionAttributeManagedObject) mo;
		httpSessionObjectMo.setBoundManagedObjectName(BOUND_NAME);
		httpSessionObjectMo.loadObjects(objectRegistry);
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof HttpSessionAttribute);

		// Ensure able to interact with HTTP session
		HttpSessionAttribute<Serializable> httpSessionObject = (HttpSessionAttribute<Serializable>) object;
		assertNull("Initially should not obtain object from session", httpSessionObject.getSessionObject());
		httpSessionObject.setSessionObject(SESSION_OBJECT);
		assertEquals("Incorrect loaded session object", SESSION_OBJECT, httpSessionObject.getSessionObject());

		this.verifyMockObjects();
	}

}