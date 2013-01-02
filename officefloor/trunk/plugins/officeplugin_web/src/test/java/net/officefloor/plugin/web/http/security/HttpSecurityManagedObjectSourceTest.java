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
package net.officefloor.plugin.web.http.security;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObject;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource.AuthenticateTaskDependencyKeys;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource.FlowKeys;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * Tests the {@link HttpSecurityManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(HttpSecurityManagedObjectSource.class);
	}

	/**
	 * Ensure correctly specifies the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpSecurity.class);
		type.addTeam("AUTHENTICATOR");
		type.addDependency(DependencyKeys.HTTP_SECURITY_SERVICE,
				HttpSecurityService.class, null);

		// Validate the type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpSecurityManagedObjectSource.class);
	}

	/**
	 * Ensure can authenticate.
	 */
	@SuppressWarnings("unchecked")
	public void testAuthenticate() throws Throwable {

		// Mock
		final TaskContext<?, AuthenticateTaskDependencyKeys, ?> taskContext = this
				.createMock(TaskContext.class);
		final AsynchronousListener listener = this
				.createMock(AsynchronousListener.class);
		final HttpSecurityService service = this
				.createMock(HttpSecurityService.class);
		final HttpSecurity expectedSecurity = this
				.createMock(HttpSecurity.class);

		// Create mock to determine if authenticated
		final boolean[] isAuthenticateTriggered = new boolean[1];
		isAuthenticateTriggered[0] = false;
		final HttpSecurityManagedObject mockManagedObject = new HttpSecurityManagedObject(
				null) {
			@Override
			public void authenticate() throws AuthenticationException {
				isAuthenticateTriggered[0] = true;
			}
		};

		// Record
		this.recordReturn(
				taskContext,
				taskContext
						.getObject(AuthenticateTaskDependencyKeys.HTTP_SECURITY_MANAGED_OBJECT),
				mockManagedObject);
		listener.notifyStarted();
		this.recordReturn(service, service.authenticate(), expectedSecurity);
		listener.notifyComplete();

		// Replay
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		final HttpSecurityManagedObjectSource source = loader
				.loadManagedObjectSource(HttpSecurityManagedObjectSource.class);
		loader.registerInvokeProcessTask(FlowKeys.AUTHENTICATE, source,
				taskContext);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setAsynchronousListener(listener);
		user.mapDependency(DependencyKeys.HTTP_SECURITY_SERVICE, service);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Assert that authenticate triggered (and then trigger to complete)
		assertTrue("authenticate should be triggered",
				isAuthenticateTriggered[0]);
		HttpSecurityManagedObject httpSecurityManagedObject = (HttpSecurityManagedObject) managedObject;
		httpSecurityManagedObject.authenticate(); // trigger the authentication

		// Verify
		this.verifyMockObjects();

		// Obtain the HTTP Security
		HttpSecurity actualSecurity = (HttpSecurity) managedObject.getObject();
		assertEquals("Incorrect HTTP security", expectedSecurity,
				actualSecurity);
	}

}