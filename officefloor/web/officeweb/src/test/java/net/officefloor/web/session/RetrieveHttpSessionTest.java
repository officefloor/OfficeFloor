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
import net.officefloor.web.session.HttpSessionManagedObject;
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
	 * Constant time to enable easier testing.
	 */
	private static final long CREATION_TIME = 100;

	/**
	 * Time to expire the {@link HttpSession}.
	 */
	private static final long EXPIRE_TIME = Long.MAX_VALUE;

	/**
	 * Ensure able to create a {@link HttpSession} immediately.
	 */
	public void testImmediateRetrieval() throws Throwable {

		// Record retrieving HTTP session
		this.record_sessionIdCookie(SESSION_ID);
		this.record_retrieve_sessionRetrieved(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFunctionality(mo, false);
	}

	/**
	 * Ensure able to retrieve {@link HttpSession} with delay retrieving the
	 * {@link HttpSession} from the {@link HttpSessionStore}.
	 */
	public void testDeplayInRetrievingFromStore() throws Throwable {

		// Record retrieving HTTP session
		this.record_sessionIdCookie(SESSION_ID);
		this.record_delay(); // retrieving from store
		this.asynchronousContext.start(null);
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.retrieveOperation.sessionRetrieved(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.verifyFunctionality(mo, false);
	}

	/**
	 * Ensure able to handle immediate failure to retrieve {@link HttpSession}
	 * from the {@link HttpSessionStore}.
	 */
	public void testImmediateFailureToRetrieveSessionFromStore() throws Throwable {

		final Exception failure = new Exception("Retrieve Session failure");

		// Record retrieving HTTP session
		this.record_sessionIdCookie(SESSION_ID);
		this.record_retrieve_failedToRetrieveSession(failure);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure to retrieve {@link HttpSession}
	 * from the {@link HttpSessionStore}.
	 */
	public void testDelayedFailureToRetrieveSessionFromStore() throws Throwable {

		final Exception failure = new Exception("Retrieve Session failure");

		// Record retrieving HTTP session
		this.record_sessionIdCookie(SESSION_ID);
		this.record_delay(); // failing to retrieve
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.retrieveOperation.failedToRetreiveSession(failure);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensures can handle an immediate Session not available.
	 */
	public void testImmediateSessionNotAvailable() throws Throwable {

		// Record retrieving HTTP session
		this.record_sessionIdCookie("Not available Session Id");
		this.record_retrieve_sessionNotAvailable();
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFunctionality(mo, true);
	}

	/**
	 * Ensure can handle delay in Session not available.
	 */
	public void testDelayInSessionNotAvailable() throws Throwable {

		// Record retrieving HTTP session
		this.record_sessionIdCookie("Not available Session Id");
		this.record_delay(); // session not available
		this.asynchronousContext.start(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
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
		assertHttpSession(SESSION_ID, CREATION_TIME, isNew, session);
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