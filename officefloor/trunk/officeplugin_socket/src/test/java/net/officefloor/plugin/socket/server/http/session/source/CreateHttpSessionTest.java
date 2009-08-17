/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.session.source;

import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.spi.HttpSessionStore;

/**
 * Tests the creation of a {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class CreateHttpSessionTest extends
		AbstractHttpSessionManagedObjectTestCase {

	/**
	 * Session Id value.
	 */
	private static final String SESSION_ID = "SESSION_ID";

	/**
	 * Constant time to enable easier testing.
	 */
	private static final long CREATION_TIME = 100;

	/**
	 * Ensure able to create a {@link HttpSession} immediately.
	 */
	public void testImmediateHttpSession() throws Throwable {

		// Create objects
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID),
				new MockHttpSessionStore(CREATION_TIME, newAttributes()));

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify functionality
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay generating the
	 * Session Id.
	 */
	public void testDelayInSessionIdGeneration() throws Throwable {

		// Create objects
		MockHttpSessionIdGenerator generator = new MockHttpSessionIdGenerator();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				generator, new MockHttpSessionStore(CREATION_TIME,
						newAttributes()));

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		generator.setSessionId(SESSION_ID);

		// Verify functionality
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay creating the
	 * {@link HttpSession} within the {@link HttpSessionStore}.
	 */
	public void testDeplayInCreationWithinStore() throws Throwable {

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		store.loadAttributes(newAttributes());

		// Verify functionality
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to create {@link HttpSession} with delay in both generating
	 * the SessionId and creating the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 */
	public void testDeplayInBothSessionIdGenerationAndCreationWithinStore()
			throws Throwable {

		// Create objects
		MockHttpSessionIdGenerator generator = new MockHttpSessionIdGenerator();
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				generator, store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		generator.setSessionId(SESSION_ID);
		store.loadAttributes(newAttributes());

		// Verify functionality
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure able to handle failure in generating the Session Id.
	 */
	public void testImmediateFailureToGenerateSessionId() throws Throwable {

		final Exception failure = new Exception("Generate Session Id failure");

		// Create objects
		MockHttpSessionIdGenerator generator = new MockHttpSessionIdGenerator();
		generator.setFailure(failure);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				generator, new MockHttpSessionStore(CREATION_TIME));

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify failure
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure in generating the Session Id.
	 */
	public void testDelayInFailureToGenerateSessionId() throws Throwable {

		final Exception failure = new Exception("Generate Session Id failure");

		// Create objects
		MockHttpSessionIdGenerator generator = new MockHttpSessionIdGenerator();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				generator, new MockHttpSessionStore(CREATION_TIME));

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		generator.setFailure(failure);

		// Verify failure
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle immediate failure to create {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	public void testImmediateFailureToCreateSessionWithinStore()
			throws Throwable {

		final Exception failure = new Exception("Create Session failure");

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		store.loadFailure(failure);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify failure
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure to create {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	public void testDelayedFailureToCreateSessionWithinStore() throws Throwable {

		final Exception failure = new Exception("Create Session failure");

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		store.loadFailure(failure);

		// Verify failure
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensures can handle an immediate detection of Session Id collision.
	 */
	public void testImmediateSessionIdCollision() throws Throwable {

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME,
				newAttributes());
		store.flagSessionIdCollision();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify functionality
		this.verifyFunctionality(mo);
	}

	/**
	 * Ensure can handle delay in Session Id collision.
	 */
	public void testDelayInSessionIdCollision() throws Throwable {

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(null);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		store.flagSessionIdCollision();
		store.loadAttributes(newAttributes());

		// Verify functionality
		this.verifyFunctionality(mo);
	}

	/**
	 * Verifies the {@link HttpSessionManagedObject}.
	 *
	 * @param mo
	 *            {@link HttpSessionManagedObject} to verify.
	 */
	private void verifyFunctionality(HttpSessionManagedObject mo)
			throws Throwable {

		// Verify the mocks
		this.verifyMockObjects();

		// Verify new Http Session
		HttpSession session = (HttpSession) mo.getObject();
		assertHttpSession(SESSION_ID, CREATION_TIME, true, session);
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

		// Verify the mocks
		this.verifyMockObjects();

		// Verify failure
		try {
			mo.getObject();
			fail("Should not be able to obtain object");
		} catch (Throwable ex) {
			assertSame("Incorrect failure", failure, ex);
		}
	}

}