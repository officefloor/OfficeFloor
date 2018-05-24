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
package net.officefloor.plugin.servlet.web.http.session;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.servlet.web.http.session.ServletHttpSessionManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link ServletHttpSessionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpSessionManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(ServletHttpSessionManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpSession.class);
		type.addDependency(DependencyKeys.SERVLET_BRIDGE, ServletBridge.class,
				null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletHttpSessionManagedObjectSource.class);
	}

	/**
	 * Ensure can source {@link HttpSession}.
	 */
	public void testSource() throws Throwable {

		final ServletBridge bridge = this.createMock(ServletBridge.class);
		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final javax.servlet.http.HttpSession httpSession = this
				.createMock(javax.servlet.http.HttpSession.class);

		// Record to ensure correct HTTP session wrapping
		this.recordReturn(bridge, bridge.getRequest(), request);
		this.recordReturn(request, request.getSession(), httpSession);
		this.recordReturn(httpSession, httpSession.getId(), "SESSION");
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ServletHttpSessionManagedObjectSource source = loader
				.loadManagedObjectSource(ServletHttpSessionManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(DependencyKeys.SERVLET_BRIDGE, bridge);
		ManagedObject mo = user.sourceManagedObject(source);

		// Obtain the object
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof HttpSession);
		HttpSession session = (HttpSession) object;

		// Ensure correct HTTP session wrapping
		assertEquals("Incorrect HTTP session", "SESSION",
				session.getSessionId());

		// Ensure correct
		this.verifyMockObjects();
	}

}