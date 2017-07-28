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
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link AnonymousHttpAuthenticationManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AnonymousHttpAuthenticationManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	@SuppressWarnings("unchecked")
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(AnonymousHttpAuthenticationManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	@SuppressWarnings("unchecked")
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpAuthentication.class);

		// Validate expected type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, AnonymousHttpAuthenticationManagedObjectSource.class);
	}

	/**
	 * Validate provides anonymous authentication.
	 */
	@SuppressWarnings("unchecked")
	public void testAuthenticate() throws Throwable {

		final HttpAuthenticateRequest<Void> request = this.createMock(HttpAuthenticateRequest.class);

		// Record request completing immediately
		request.authenticationComplete();

		// Test
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		AnonymousHttpAuthenticationManagedObjectSource<?, ?> source = loader
				.loadManagedObjectSource(AnonymousHttpAuthenticationManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the authentication
		HttpAuthentication<HttpSecurity, Void> authentication = (HttpAuthentication<HttpSecurity, Void>) managedObject
				.getObject();
		assertNotNull("Should have authentication", authentication);

		// Trigger authentication
		authentication.authenticate(request);
		assertNull("Should not provide HTTP Security as anonymous", authentication.getHttpSecurity());

		// Trigger authentication again without request
		authentication.authenticate(null);
		assertNull("Should again be anonymous", authentication.getHttpSecurity());

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Validate logout does nothing.
	 */
	@SuppressWarnings("unchecked")
	public void testLogout() throws Throwable {

		final HttpLogoutRequest request = this.createMock(HttpLogoutRequest.class);

		// Record request completing immediately
		request.logoutComplete(null);

		// Test
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		AnonymousHttpAuthenticationManagedObjectSource<?, ?> source = loader
				.loadManagedObjectSource(AnonymousHttpAuthenticationManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the authentication
		HttpAuthentication<HttpSecurity, Void> authentication = (HttpAuthentication<HttpSecurity, Void>) managedObject
				.getObject();
		assertNotNull("Should have authentication", authentication);

		// Trigger logout
		authentication.logout(request);
		assertNull("Should stay as anonymous", authentication.getHttpSecurity());

		// Trigger logout again without request
		authentication.logout(null);
		assertNull("Should again be anonymous", authentication.getHttpSecurity());

		// Verify functionality
		this.verifyMockObjects();
	}

}