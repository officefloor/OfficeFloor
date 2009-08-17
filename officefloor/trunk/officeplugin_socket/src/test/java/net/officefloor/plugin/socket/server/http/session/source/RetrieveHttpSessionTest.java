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
 * Tests retrieving an existing {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class RetrieveHttpSessionTest extends
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
	public void testImmediateRetrieval() throws Throwable {

		// Create objects
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID),
				new MockHttpSessionStore(CREATION_TIME, newAttributes()));

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(SESSION_ID);

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify functionality
		this.verifyFunctionality(mo, false);
	}

	/**
	 * Ensure able to retrieve {@link HttpSession} with delay retrieving the
	 * {@link HttpSession} from the {@link HttpSessionStore}.
	 */
	public void testDeplayInRetrievingFromStore() throws Throwable {

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(SESSION_ID);
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		store.loadAttributes(newAttributes());

		// Verify functionality
		this.verifyFunctionality(mo, false);
	}

	/**
	 * Ensure able to handle immediate failure to retrieve {@link HttpSession}
	 * from the {@link HttpSessionStore}.
	 */
	public void testImmediateFailureToRetrieveSessionFromStore()
			throws Throwable {

		final Exception failure = new Exception("Retrieve Session failure");

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		store.loadFailure(failure);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(SESSION_ID);

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify failure
		this.verifyFailure(mo, failure);
	}

	/**
	 * Ensure able to handle delayed failure to retrieve {@link HttpSession}
	 * from the {@link HttpSessionStore}.
	 */
	public void testDelayedFailureToRetrieveSessionFromStore() throws Throwable {

		final Exception failure = new Exception("Retrieve Session failure");

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(SESSION_ID);
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
	 * Ensures can handle an immediate Session not available.
	 */
	public void testImmediateSessionNotAvailable() throws Throwable {

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME,
				newAttributes());
		store.flagSessionNotAvailable();
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie("Not available Session Id");

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);

		// Verify functionality
		this.verifyFunctionality(mo, true);
	}

	/**
	 * Ensure can handle delay in Session not available.
	 */
	public void testDelayInSessionNotAvailable() throws Throwable {

		// Create objects
		MockHttpSessionStore store = new MockHttpSessionStore(CREATION_TIME);
		HttpSessionManagedObject mo = this.createHttpSessionManagedObject(
				new MockHttpSessionIdGenerator(SESSION_ID), store);

		// Record creating HTTP session
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie("Not available Session Id");
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Load the managed object
		this.replayMockObjects();
		this.startCoordination(mo);
		store.flagSessionNotAvailable();
		store.loadAttributes(newAttributes());

		// Verify functionality
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
	private void verifyFunctionality(HttpSessionManagedObject mo, boolean isNew)
			throws Throwable {

		// Verify the mocks
		this.verifyMockObjects();

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