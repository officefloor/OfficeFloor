/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * Tests storing the {@link HttpSession} via the
 * {@link HttpSessionAdministration} interface.
 *
 * @author Daniel Sagenschneider
 */
public class StoreHttpSessionTest extends AbstractHttpSessionManagedObjectTestCase {

	/**
	 * Session Id value.
	 */
	private static final String SESSION_ID = "SESSION_ID";

	/**
	 * Time to expire the {@link HttpSession}.
	 */
	private static final Instant EXPIRE_TIME = MOCK_CURRENT_TIME.plus(2000, ChronoUnit.SECONDS);

	/**
	 * {@link HttpSession}.
	 */
	private HttpSession httpSession;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final MockServerHttpConnection connection = MockHttpServer
			.mockConnection(MockHttpServer.mockRequest().cookie(SESSION_ID_COOKIE_NAME, SESSION_ID));

	/**
	 * Ensure can immediately store the {@link HttpSession}.
	 */
	public void testImmediateStore() throws Throwable {

		// Record storing
		this.record_instantiate();
		this.record_store_sessionStored();

		// Store
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.store();
		assertTrue("Should be immediately stored", admin.isOperationComplete());
		this.verifyFunctionality(SESSION_ID, EXPIRE_TIME);
	}

	/**
	 * Ensure can delay storing the {@link HttpSession}.
	 */
	public void testDelayStore() throws Throwable {

		// Record storing
		this.record_instantiate();
		this.record_delay(); // storing
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Store
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.store();
		assertFalse("Should delay storing", admin.isOperationComplete());
		this.ensureHttpSessionNotAlterable(this.httpSession);
		this.storeOperation.sessionStored();
		assertTrue("Should store after delay", admin.isOperationComplete());
		this.verifyFunctionality(SESSION_ID, EXPIRE_TIME);
	}

	/**
	 * Ensure can handle immediately failing to store the {@link HttpSession}.
	 */
	public void testImmediateFailToStore() throws Throwable {

		final Exception failure = new Exception("Failure to store");

		// Record storing
		this.record_instantiate();
		this.record_store_failedToStoreSession(failure);

		// Store
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		try {
			admin.store();
			fail("Should not successfully store");
		} catch (Throwable ex) {
			assertSame("Incorrect cause of failure to store", failure, ex);
		}
		this.verifyFailure(admin, failure);
	}

	/**
	 * Ensure can delay failing to store the {@link HttpSession}.
	 */
	public void testDelayFailToStore() throws Throwable {

		final Exception failure = new Exception("Failure to store");

		// Record storing
		this.record_instantiate();
		this.record_delay(); // storing
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Store
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		admin.store();
		assertFalse("Should delay storing", admin.isOperationComplete());
		this.ensureHttpSessionNotAlterable(this.httpSession);
		this.storeOperation.failedToStoreSession(failure);
		this.verifyFailure(admin, failure);
	}

	/**
	 * Ensure can change the expire time for the {@link HttpSession}.
	 */
	public void testChangeExpireTime() throws Throwable {

		final Instant NEW_EXPIRE_TIME = MOCK_CURRENT_TIME.plus(10000, ChronoUnit.SECONDS);

		// Record changing expire time and storing
		this.record_instantiate();
		this.record_store_sessionStored();

		// Change expire time and store
		this.replayMockObjects();
		HttpSessionAdministration admin = this.createHttpSessionAdministration();
		this.httpSession.setExpireTime(NEW_EXPIRE_TIME);
		admin.store();
		assertTrue("Should be immediately stored", admin.isOperationComplete());
		this.verifyFunctionality(SESSION_ID, NEW_EXPIRE_TIME);
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
	 */
	private void verifyFunctionality(String newSessionId, Instant expires) {
		// Verify the mocks and operations
		this.verifyOperations();

		// Ensure can now alter session
		this.httpSession.setAttribute("TEST", "ALTERED");
		this.httpSession.removeAttribute("TEST");

		// Verify response with session cookie
		MockHttpResponse response = this.connection.send(null);
		if (newSessionId == null) {
			// Ensure cookie is not updated
			assertEquals("Should not update session cookie", 0, response.getCookies().size());
		} else {
			// Ensure sending new session id
			assertEquals("Should provide session cookie", 1, response.getCookies().size());
			response.assertCookie(
					MockHttpServer.mockResponseCookie(SESSION_ID_COOKIE_NAME, newSessionId).setExpires(expires));
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

		// Ensure propagate the failure
		try {
			admin.isOperationComplete();
			fail("Should propagate the failure");
		} catch (Throwable ex) {
			assertSame("Incorrect failure propagated", failure, ex);
		}
	}

	/**
	 * Ensures the {@link HttpSession} is not alterable.
	 *
	 * @param session
	 *            {@link HttpSession}.
	 */
	private void ensureHttpSessionNotAlterable(HttpSession session) {

		// Ensure can obtain details of session while it is being stored
		session.getSessionId();
		session.isNew();
		session.getCreationTime();
		session.getAttribute("TEST");
		session.getAttributeNames();
		session.getHttpSessionAdministration();

		// Should not be able to alter session however while being stored
		this.ensureHttpSessionNotAlterable(session, new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.setAttribute("TEST", "VALUE");
			}
		});
		this.ensureHttpSessionNotAlterable(session, new HttpSessionInvocation() {
			@Override
			public void invoke(HttpSession session) {
				session.removeAttribute("TEST");
			}
		});
	}

	/**
	 * Ensures the {@link HttpSession} is not alterable.
	 *
	 * @param session
	 *            {@link HttpSession}.
	 * @param invocation
	 *            {@link HttpSessionInvocation}.
	 */
	private void ensureHttpSessionNotAlterable(HttpSession session, HttpSessionInvocation invocation) {
		try {
			invocation.invoke(session);
			fail("Should not be able to alter HTTP session");
		} catch (StoringSessionHttpException ex) {
			assertNull("Should never be cause of not alterable", ex.getCause());
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
