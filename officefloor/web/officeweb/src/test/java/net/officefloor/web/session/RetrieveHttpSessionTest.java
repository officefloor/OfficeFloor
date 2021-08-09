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

import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.session.spi.HttpSessionStore;

/**
 * Tests retrieving an existing {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class RetrieveHttpSessionTest extends AbstractHttpSessionManagedObjectTestCase {

	/**
	 * Session Id value.
	 */
	private static final String SESSION_ID = "SESSION_ID";

	/**
	 * Time to expire the {@link HttpSession}.
	 */
	private static final Instant EXPIRE_TIME = MOCK_CURRENT_TIME.plus(2000, ChronoUnit.SECONDS);

	/**
	 * {@link MockServerHttpConnection}.
	 */
	private MockServerHttpConnection connection = MockHttpServer
			.mockConnection(MockHttpServer.mockRequest().cookie(SESSION_ID_COOKIE_NAME, SESSION_ID));

	/**
	 * Ensure able to create a {@link HttpSession} immediately.
	 */
	public void testImmediateRetrieval() throws Throwable {

		// Record retrieving HTTP session
		this.record_retrieve_sessionRetrieved(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.verifyFunctionality(mo, false);
	}

	/**
	 * Ensure able to retrieve {@link HttpSession} with delay retrieving the
	 * {@link HttpSession} from the {@link HttpSessionStore}.
	 */
	public void testDeplayInRetrievingFromStore() throws Throwable {

		// Record retrieving HTTP session
		this.record_delay(); // retrieving from store
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.retrieveOperation.sessionRetrieved(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
		this.verifyFunctionality(mo, false);
	}

	/**
	 * Ensure able to handle immediate failure to retrieve {@link HttpSession}
	 * from the {@link HttpSessionStore}.
	 */
	public void testImmediateFailureToRetrieveSessionFromStore() throws Throwable {

		final Exception failure = new Exception("Retrieve Session failure");

		// Record retrieving HTTP session
		this.record_retrieve_failedToRetrieveSession(failure);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure to retrieve {@link HttpSession}
	 * from the {@link HttpSessionStore}.
	 */
	public void testDelayedFailureToRetrieveSessionFromStore() throws Throwable {

		final Exception failure = new Exception("Retrieve Session failure");

		// Record retrieving HTTP session
		this.record_delay(); // failing to retrieve
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.retrieveOperation.failedToRetreiveSession(failure);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensures can handle an immediate Session not available.
	 */
	public void testImmediateSessionNotAvailable() throws Throwable {

		// Create not available session request
		this.connection = MockHttpServer.mockConnection(
				MockHttpServer.mockRequest().cookie(SESSION_ID_COOKIE_NAME, "Not available Session Id"));

		// Record retrieving HTTP session
		this.record_retrieve_sessionNotAvailable();
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.verifyFunctionality(mo, true);
	}

	/**
	 * Ensure can handle delay in Session not available.
	 */
	public void testDelayInSessionNotAvailable() throws Throwable {

		// Create not available session request
		this.connection = MockHttpServer.mockConnection(
				MockHttpServer.mockRequest().cookie(SESSION_ID_COOKIE_NAME, "Not available Session Id"));

		// Record retrieving HTTP session
		this.record_delay(); // session not available
		this.asynchronousContext.start(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, newAttributes());
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(this.connection);
		this.retrieveOperation.sessionNotAvailable();
		this.verifyFunctionality(mo, true);
	}

	/**
	 * Verifies the {@link HttpSessionManagedObject}.
	 *
	 * @param mo
	 *            {@link HttpSessionManagedObject} to verify.
	 * @param isNew
	 *            Flag indicating if {@link HttpSession} is new.
	 */
	private void verifyFunctionality(HttpSessionManagedObject mo, boolean isNew) throws Throwable {

		// Verify the mocks and operations
		this.verifyOperations();

		// Verify new Http Session
		HttpSession session = (HttpSession) mo.getObject();
		assertHttpSession(SESSION_ID, MOCK_CURRENT_TIME, isNew, session);
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

		// Verify the mocks and operations
		this.verifyOperations();

		// Verify failure
		try {
			mo.getObject();
			fail("Should not be able to obtain object");
		} catch (Throwable ex) {
			assertSame("Incorrect failure", failure, ex);
		}
	}

}
