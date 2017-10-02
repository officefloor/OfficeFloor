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
package net.officefloor.web.session;

import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.HttpSessionAdministration;
import net.officefloor.web.session.HttpSessionManagedObject;
import net.officefloor.web.session.InvalidatedSessionHttpException;

/**
 * Tests invalidating the {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class InvalidateHttpSessionTest extends AbstractHttpSessionManagedObjectTestCase {

	/**
	 * Instantiated Session Id value.
	 */
	private static final String SESSION_ID = "SESSION_ID";

	/**
	 * Session Id of new {@link HttpSession}.
	 */
	private static final String NEW_SESSION_ID = "NEW_SESSION";

	/**
	 * Instantiated time to enable easier testing.
	 */
	private static final long CREATION_TIME = 100;

	/**
	 * Time to expire the {@link HttpSession}.
	 */
	private static final long EXPIRE_TIME = Long.MAX_VALUE;

	/**
	 * Creation time of new {@link HttpSession}.
	 */
	private static final long NEW_CREATION_TIME = 200;

	/**
	 * {@link HttpSession}.
	 */
	private HttpSession httpSession;

	/**
	 * Ensures invalidate without creating a new {@link HttpSession}.
	 */
	public void testInvalidateWithoutCreatingNewSession() throws Throwable {

		// Record
		this.record_instantiate();
		this.record_invalidate_sessionInvalidated();
		this.record_cookie_addSessionId(true, "", 0);

		// Invalidate
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.invalidate(false);
		this.verifyFunctionality(admin, false);
	}

	/**
	 * Ensure can invalidate the {@link HttpSession} and immediately create
	 * another {@link HttpSession}.
	 */
	public void testInvalidateAndImmediatelyCreateNewSession() throws Throwable {

		// Record
		this.record_instantiate();
		this.record_invalidate_sessionInvalidated();
		this.record_generate_setSessionId(NEW_SESSION_ID);
		this.record_create_sessionCreated(NEW_CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(true, NEW_SESSION_ID, EXPIRE_TIME);

		// Invalidate
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.invalidate(true);
		this.verifyFunctionality(admin, true);
	}

	/**
	 * Ensures handle delay in invalidating {@link HttpSession}.
	 */
	public void testDelayInvalidatingSession() throws Throwable {

		// Record
		this.record_instantiate();
		this.record_delay(); // invalidating session
		this.asynchronousContext.start(null);
		this.record_generate_setSessionId(NEW_SESSION_ID);
		this.record_create_sessionCreated(NEW_CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(true, NEW_SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Invalidate
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.invalidate(true);
		assertFalse("Delay invalidating", admin.isOperationComplete());
		this.ensureHttpSessionInvalidated(null);
		this.invalidateOperation.sessionInvalidated();
		this.verifyFunctionality(admin, true);
	}

	/**
	 * Ensure can handle immediately failing to invalidate.
	 */
	public void testImediatelyFailInvalidate() throws Throwable {

		final Exception failure = new Exception("Failed to invalidate");

		// Record
		this.record_instantiate();
		this.record_invalidate_failedToInvalidateSession(failure);

		// Invalidate
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		try {
			admin.invalidate(true);
		} catch (Exception ex) {
			assertSame("Incorrect cause of failure", failure, ex);
		}
		this.verifyFailure(admin, failure);
	}

	/**
	 * Ensure can handle delayed failure to invalidate.
	 */
	public void testDelayFailInvalidate() throws Throwable {

		final Exception failure = new Exception("Failed to invalidate");

		// Record
		this.record_instantiate();
		this.record_delay(); // invalidating
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Invalidate
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.invalidate(true);
		assertFalse("Delay invalidating", admin.isOperationComplete());
		this.ensureHttpSessionInvalidated(null);
		this.invalidateOperation.failedToInvalidateSession(failure);
		this.verifyFailure(admin, failure);
	}

	/**
	 * Records instantiating the {@link HttpSession}.
	 */
	private void record_instantiate() {
		this.record_sessionIdCookie(SESSION_ID);
		this.record_retrieve_sessionRetrieved(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(false, SESSION_ID, EXPIRE_TIME);
	}

	/**
	 * Ensures the {@link HttpSessionAdministration} is created.
	 *
	 * @return {@link HttpSessionAdministration}.
	 */
	private HttpSessionAdministration createHttpSessionAdministration() throws Throwable {
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.httpSession = (HttpSession) mo.getObject();
		assertHttpSession(SESSION_ID, CREATION_TIME, false, this.httpSession);
		return this.httpSession.getHttpSessionAdministration();
	}

	/**
	 * Verifies the functionality.
	 *
	 * @param admin
	 *            {@link HttpSessionAdministration}.
	 * @param isCreateNew
	 *            Flag indicating if expected to create a new
	 *            {@link HttpSession}.
	 */
	private void verifyFunctionality(HttpSessionAdministration admin, boolean isCreateNew) throws Throwable {
		// Verify the mocks and operations
		this.verifyOperations();

		// Operations should be complete
		assertTrue("Operation should be complete", admin.isOperationComplete());

		// Handle whether should create a new HTTP session
		if (isCreateNew) {
			// Ensure created new HTTP session
			assertHttpSession(NEW_SESSION_ID, NEW_CREATION_TIME, isCreateNew, this.httpSession);
		} else {
			// Ensure HTTP session invalidated (without cause)
			this.ensureHttpSessionInvalidated(null);
		}
	}

	/**
	 * Verifies the failure.
	 *
	 * @param admin
	 *            {@link HttpSessionAdministration}.
	 * @param failure
	 *            Expected failure.
	 */
	private void verifyFailure(HttpSessionAdministration admin, Throwable failure) {
		// Verify the mocks and operations
		this.verifyOperations();

		// Ensure failure
		try {
			admin.isOperationComplete();
			fail("Expected to propagate failure");
		} catch (Throwable ex) {
			assertSame("Incorrect cause of failure", failure, ex);
		}

		// Ensure report failure from HTTP session
		this.ensureHttpSessionInvalidated(failure);
	}

	/**
	 * Ensures the {@link HttpSession} is invalidated.
	 *
	 * @param failure
	 *            Potential failure in invalidating. May be <code>null</code>.
	 */
	private void ensureHttpSessionInvalidated(Throwable failure) {

		// Session Id
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.getSessionId();
			}
		}, failure);

		// Is new
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.isNew();
			}
		}, failure);

		// Creation time
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.getCreationTime();
			}
		}, failure);

		// Get attribute
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.getAttribute("TEST");
			}
		}, failure);

		// Attributes names
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.getAttributeNames();
			}
		}, failure);

		// Set attribute
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.setAttribute("TEST", "ATTRIBUTE");
			}
		}, failure);

		// Remove attribute
		this.ensureHttpSessionInvalidated(new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.removeAttribute("TEST");
			}
		}, failure);
	}

	/**
	 * Ensures the {@link HttpSession} is invalid.
	 *
	 * @param invocation
	 *            {@link HttpSessionInvocation}.
	 * @param failure
	 *            Potential failure in invalidating. May be <code>null</code>.
	 */
	private void ensureHttpSessionInvalidated(HttpSessionInvocation invocation, Throwable failure) {
		try {
			invocation.invoke(this.httpSession);
			fail("Should be invalid HTTP session");
		} catch (InvalidatedSessionHttpException ex) {
			assertSame("Incorrect cause of invalidating failure", failure, ex.getCause());
		}
	}

	/**
	 * Invocation of a method on the {@link HttpSession}.
	 */
	private interface HttpSessionInvocation {

		/**
		 * Invokes the method on the {@link HttpSession}.
		 *
		 * @param session
		 *            {@link HttpSession}.
		 */
		void invoke(HttpSession session);
	}

}