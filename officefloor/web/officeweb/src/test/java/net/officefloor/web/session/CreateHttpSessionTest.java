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
	public void testImmediateCreation() throws Throwable {

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay generating the
	 * Session Id.
	 */
	public void testDelayInSessionIdGeneration() throws Throwable {

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_delay(); // creating session id
		this.asynchronousContext.start(null);
		this.record_create_sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.freshHttpSession.setSessionId(SESSION_ID);
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay creating the
	 * {@link HttpSession} within the {@link HttpSessionStore}.
	 */
	public void testDeplayInCreationWithinStore() throws Throwable {

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_delay(); // creating within store
		this.asynchronousContext.start(null);
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.createOperation.sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay in both generating
	 * the SessionId and creating the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 */
	public void testDeplayInBothSessionIdGenerationAndCreationWithinStore() throws Throwable {

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_delay(); // creating session id
		this.asynchronousContext.start(null);
		this.record_delay(); // creating within store
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.freshHttpSession.setSessionId(SESSION_ID);
		this.createOperation.sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to handle failure in generating the Session Id.
	 */
	public void testImmediateFailureToGenerateSessionId() throws Throwable {

		final Exception failure = new Exception("Generate Session Id failure");

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_generate_failedToGenerateSessionId(failure);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure in generating the Session Id.
	 */
	public void testDelayInFailureToGenerateSessionId() throws Throwable {

		final Exception failure = new Exception("Generate Session Id failure");

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_delay(); // creating session id
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
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
		this.record_sessionIdCookie(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_failedToCreateSession(failure);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure to create {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	public void testDelayedFailureToCreateSessionWithinStore() throws Throwable {

		final Exception failure = new Exception("Create Session failure");

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_delay(); // creating within store
		this.asynchronousContext.start(null);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.createOperation.failedToCreateSession(failure);
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensures can handle an immediate detection of Session Id collision.
	 */
	public void testImmediateSessionIdCollision() throws Throwable {

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_generate_setSessionId("SESSION_ID_COLLISION");
		this.record_create_sessionIdCollision();
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure can handle delay in Session Id collision.
	 */
	public void testDelayInSessionIdCollision() throws Throwable {

		// Record creating HTTP session
		this.record_sessionIdCookie(null);
		this.record_generate_setSessionId("SESSION_ID_COLLISION");
		this.record_delay(); // detecting collision
		this.asynchronousContext.start(null);
		this.record_generate_setSessionId(SESSION_ID);
		this.record_create_sessionCreated(CREATION_TIME, EXPIRE_TIME, newAttributes());
		this.record_cookie_addSessionId(SESSION_ID, EXPIRE_TIME);
		this.asynchronousContext.complete(null);

		// Load the managed object
		this.replayMockObjects();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject();
		this.startCoordination(mo);
		this.createOperation.sessionIdCollision();
		this.verifyFunctionality(mo);
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

		// Verify new Http Session
		HttpSession session = (HttpSession) mo.getObject();
		assertHttpSession(SESSION_ID, CREATION_TIME, true, session);
		assertEquals("Incorrect Token name", SESSION_ID_COOKIE_NAME, session.getTokenName());
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
	}

}