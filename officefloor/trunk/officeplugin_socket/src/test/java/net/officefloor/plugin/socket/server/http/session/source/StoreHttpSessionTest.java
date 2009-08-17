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

import java.util.Map;

import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.HttpSessionAdministration;
import net.officefloor.plugin.socket.server.http.session.spi.StoreHttpSessionOperation;

/**
 * Tests storing the {@link HttpSession} via the
 * {@link HttpSessionAdministration} interface.
 *
 * @author Daniel Sagenschneider
 */
public class StoreHttpSessionTest extends
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
	 * {@link MockHttpSessionIdGenerator}.
	 */
	private final MockHttpSessionIdGenerator generator = new MockHttpSessionIdGenerator(
			SESSION_ID);

	/**
	 * Attributes of the {@link HttpSession}.
	 */
	private final Map<String, Object> attributes = newAttributes();

	/**
	 * {@link MockHttpSessionStore}.
	 */
	private final MockHttpSessionStore store = new MockHttpSessionStore(
			CREATION_TIME, this.attributes);

	/**
	 * {@link HttpSessionManagedObject}.
	 */
	private final HttpSessionManagedObject mo = this
			.createHttpSessionManagedObject(this.generator, this.store);;

	/**
	 * Ensure can immediately store the {@link HttpSession}.
	 */
	public void testImmediateStore() throws Throwable {

		// Setup to immediately store
		this.store.setStoreImmediately(true);

		// Record
		this.record_Create();

		// Run test
		this.replayMockObjects();
		HttpSessionAdministration admin = this
				.createHttpSessionAdministration();
		admin.store();
		assertTrue("Should be immediately stored", admin.isOperationComplete());

		// Verify
		this.verifyFunctionality();
	}

	/**
	 * Ensure can delay storing the {@link HttpSession}.
	 */
	public void testDelayStore() throws Throwable {

		// Setup to delay storage
		this.store.setStoreImmediately(false);

		// Record
		this.record_Create();
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Run test
		this.replayMockObjects();
		HttpSessionAdministration admin = this
				.createHttpSessionAdministration();
		admin.store();
		assertFalse("Should delay storing", admin.isOperationComplete());
		this.store.getStoreHttpSessionOperation().sessionStored();
		assertTrue("Should store after delay", admin.isOperationComplete());

		// Verify
		this.verifyFunctionality();
	}

	/**
	 * Ensure can handle immediately failing to store the {@link HttpSession}.
	 */
	public void testImmediateFailToStore() throws Throwable {

		final Exception failure = new Exception("Failure to store");

		// Setup to fail immediately
		this.store.loadFailure(failure);

		// Record
		this.record_Create();

		// Run test
		this.replayMockObjects();
		HttpSessionAdministration admin = this
				.createHttpSessionAdministration();
		try {
			admin.store();
			fail("Should not successfully store");
		} catch (Throwable ex) {
			assertSame("Incorrect cause of failure to store", failure, ex);
		}

		// Ensure also propagates the failure
		try {
			admin.isOperationComplete();
			fail("Should propagate the failure");
		} catch (Throwable ex) {
			assertSame("Incorrect failure propagated", failure, ex);
		}

		// Verify
		this.verifyFunctionality();
	}

	/**
	 * Ensure can delay failing to store the {@link HttpSession}.
	 */
	public void testDelayFailToStore() throws Throwable {

		final Exception failure = new Exception("Failure to store");

		// Setup to delay storage
		this.store.setStoreImmediately(false);

		// Record
		this.record_Create();
		this.asynchronousListener.notifyStarted();
		this.asynchronousListener.notifyComplete();

		// Run test
		this.replayMockObjects();
		HttpSessionAdministration admin = this
				.createHttpSessionAdministration();
		admin.store();
		assertFalse("Should delay storing", admin.isOperationComplete());
		this.store.loadFailure(failure);

		// Ensure propagate the failure
		try {
			admin.isOperationComplete();
			fail("Should propagate the failure");
		} catch (Throwable ex) {
			assertSame("Incorrect failure propagated", failure, ex);
		}

		// Verify
		this.verifyFunctionality();
	}

	/**
	 * Records creating the {@link HttpSession}.
	 */
	private void record_Create() {
		this.record_obtainHttpRequest();
		this.record_obtainSessionIdCookie(SESSION_ID);
	}

	/**
	 * Ensures the {@link HttpSessionAdministration} is created.
	 *
	 * @return {@link HttpSessionAdministration}.
	 */
	private HttpSessionAdministration createHttpSessionAdministration()
			throws Throwable {
		this.startCoordination(this.mo);
		HttpSession session = (HttpSession) this.mo.getObject();
		assertHttpSession(SESSION_ID, CREATION_TIME, false, session);
		return (HttpSessionAdministration) session;
	}

	/**
	 * Verifies the functionality.
	 */
	private void verifyFunctionality() {

		// Verify the mocks
		this.verifyMockObjects();

		// Ensure stored object
		StoreHttpSessionOperation operation = this.store
				.getStoreHttpSessionOperation();
		assertEquals("Incorrect session id", SESSION_ID, operation
				.getSessionId());
		assertEquals("Incorrect creation time", CREATION_TIME, operation
				.getCreationTime());
		assertSame("Incorrect store attributes", this.attributes, operation
				.getAttributes());
	}

}