/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.session;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;

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
	 * Time to expire the {@link HttpSession}.
	 */
	private static final Instant EXPIRE_TIME = MOCK_CURRENT_TIME.plus(1000, ChronoUnit.SECONDS);

	/**
	 * Creation time of new {@link HttpSession}.
	 */
	private static final Instant NEW_CREATION_TIME = MOCK_CURRENT_TIME.plus(200, ChronoUnit.SECONDS);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final MockServerHttpConnection connection = MockHttpServer
			.mockConnection(MockHttpServer.mockRequest().cookie(SESSION_ID_COOKIE_NAME, SESSION_ID));

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
		this.record_retrieve_sessionRetrieved(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
	}

	/**
	 * Ensures the {@link HttpSessionAdministration} is created.
	 *
	 * @return {@link HttpSessionAdministration}.
	 */
	private HttpSessionAdministration createHttpSessionAdministration() throws Throwable {
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.httpSession = (HttpSession) mo.getObject();
		assertHttpSession(SESSION_ID, MOCK_CURRENT_TIME, false, this.httpSession);
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

			// Ensure response contains session cookie
			MockHttpResponse response = this.connection.send(null);
			assertEquals("Should only be the session cookie", 1, response.getCookies().size());
			response.assertCookie(
					MockHttpServer.mockResponseCookie(SESSION_ID_COOKIE_NAME, NEW_SESSION_ID).setExpires(EXPIRE_TIME));

		} else {
			// Ensure HTTP session invalidated (without cause)
			this.ensureHttpSessionInvalidated(null);

			// Ensure expire HTTP Cookie
			MockHttpResponse response = this.connection.send(null);
			assertEquals("Should only be the session cookie", 1, response.getCookies().size());
			response.assertCookie(
					MockHttpServer.mockResponseCookie(SESSION_ID_COOKIE_NAME, SESSION_ID).setExpires(Instant.EPOCH));
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
