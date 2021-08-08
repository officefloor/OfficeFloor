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
import net.officefloor.web.session.spi.HttpSessionStore;

/**
 * Tests the creation of a {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class CreateHttpSessionTest extends AbstractHttpSessionManagedObjectTestCase {

	/**
	 * Session Id value.
	 */
	private static final String SESSION_ID = "SESSION_ID";

	/**
	 * Time to expire the {@link HttpSession} (30 days).
	 */
	private static final long EXPIRE_TIME_IN_SECONDS = 30 * 24 * 60 * 60;

	/**
	 * Expire time.
	 */
	private static final Instant EXPIRE_TIME = MOCK_CURRENT_TIME.plus(EXPIRE_TIME_IN_SECONDS, ChronoUnit.SECONDS);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final MockServerHttpConnection connection = MockHttpServer.mockConnection();

	/**
	 * Ensure able to create a {@link HttpSession} immediately.
	 */
	public void testImmediateCreation() throws Throwable {

		// Record creating HTTP session
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());

		// Load the managed object (triggering creation of session)
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay generating the
	 * Session Id.
	 */
	public void testDelayInSessionIdGeneration() throws Throwable {

		// Record creating HTTP session
		this.record_delay(); // creating session id
		this.asynchronousContext.start(null);
		this.record_create_sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.freshHttpSession.setSessionId(SESSION_ID);
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay creating the
	 * {@link HttpSession} within the {@link HttpSessionStore}.
	 */
	public void testDeplayInCreationWithinStore() throws Throwable {

		// Record creating HTTP session
		this.record_generate_setSessionId(SESSION_ID);
		this.record_delay(); // creating within store
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.createOperation.sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay in both generating
	 * the SessionId and creating the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 */
	public void testDeplayInBothSessionIdGenerationAndCreationWithinStore() throws Throwable {

		// Record creating HTTP session
		this.record_delay(); // creating session id
		this.asynchronousContext.start(null);
		this.record_delay(); // creating within store
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.freshHttpSession.setSessionId(SESSION_ID);
		this.createOperation.sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to handle failure in generating the Session Id.
	 */
	public void testImmediateFailureToGenerateSessionId() throws Throwable {

		final Exception failure = new Exception("Generate Session Id failure");

		// Record creating HTTP session
		this.record_generate_failedToGenerateSessionId(failure);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure in generating the Session Id.
	 */
	public void testDelayInFailureToGenerateSessionId() throws Throwable {

		final Exception failure = new Exception("Generate Session Id failure");

		// Record creating HTTP session
		this.record_delay(); // creating session id
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.freshHttpSession.failedToGenerateSessionId(failure);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle immediate failure to create {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	public void testImmediateFailureToCreateSessionWithinStore() throws Throwable {

		final Exception failure = new Exception("Create Session failure");

		// Record creating HTTP session
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_failedToCreateSession(failure);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure to create {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	public void testDelayedFailureToCreateSessionWithinStore() throws Throwable {

		final Exception failure = new Exception("Create Session failure");

		// Record creating HTTP session
		this.record_generate_setSessionId(SESSION_ID);
		this.record_delay(); // creating within store
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.createOperation.failedToCreateSession(failure);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensures can handle an immediate detection of Session Id collision.
	 */
	public void testImmediateSessionIdCollision() throws Throwable {

		// Record creating HTTP session
		this.record_generate_setSessionId("SESSION_ID_COLLISION");
		this.record_create_sessionIdCollision();
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure can handle delay in Session Id collision.
	 */
	public void testDelayInSessionIdCollision() throws Throwable {

		// Record creating HTTP session
		this.record_generate_setSessionId("SESSION_ID_COLLISION");
		this.record_delay(); // detecting collision
		this.asynchronousContext.start(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.createOperation.sessionIdCollision();
		this.verifyFunctionality(mo);
	}

	/**
	 * Creates the {@link HttpSessionManagedObject}.
	 * 
	 * @return {@link HttpSessionManagedObject}.
	 */
	private HttpSessionManagedObject createHttpSessionManagedObject() {
		return this.createHttpSessionManagedObject(this.connection);
	}

	/**
	 * Verifies the {@link HttpSessionManagedObject}.
	 * 
	 * @param mo
	 *            {@link HttpSessionManagedObject} to verify.
	 */
	private void verifyFunctionality(HttpSessionManagedObject mo) throws Throwable {

		// Verify the operations
		this.verifyOperations();

		// Verify new HTTP Session
		HttpSession session = (HttpSession) mo.getObject();
		assertHttpSession(SESSION_ID, MOCK_CURRENT_TIME, true, session);
		assertEquals("Incorrect Token name", SESSION_ID_COOKIE_NAME, session.getTokenName());

		// Ensure response contains session cookie
		MockHttpResponse response = this.connection.send(null);
		response.assertCookie(
				MockHttpServer.mockResponseCookie(SESSION_ID_COOKIE_NAME, SESSION_ID).setExpires(EXPIRE_TIME));
	}

	/**
	 * Verifies failure to load the {@link HttpSession}.
	 * 
	 * @param mo
	 *            {@link HttpSessionManagedObject} to verify.
	 * @param failure
	 *            Expected cause of the failure.
	 */
	private void verifyFailure(HttpSessionManagedObject mo, Throwable failure) {

		// Verify the operations
		this.verifyOperations();

		// Verify failure
		try {
			mo.getObject();
			fail("Should not be able to obtain object");
		} catch (Throwable ex) {
			assertSame("Incorrect failure", failure, ex);
		}

		// Ensure response does not contain session cookie
		MockHttpResponse response = this.connection.send(null);
		assertEquals("Should not have cookies (particularly session cookie)", 0, response.getCookies().size());
	}

}
