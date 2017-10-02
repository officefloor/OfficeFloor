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
package net.officefloor.web.session.store;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.spi.CreateHttpSessionOperation;
import net.officefloor.web.session.spi.HttpSessionStore;
import net.officefloor.web.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.web.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.web.session.spi.StoreHttpSessionOperation;
import net.officefloor.web.session.store.MemoryHttpSessionStore;

/**
 * Tests the {@link MemoryHttpSessionStateTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class MemoryHttpSessionStateTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link CreateHttpSessionOperation}.
	 */
	private final CreateHttpSessionOperation createOperation = this
			.createMock(CreateHttpSessionOperation.class);

	/**
	 * Mock {@link StoreHttpSessionOperation}.
	 */
	private final StoreHttpSessionOperation storeOperation = this
			.createMock(StoreHttpSessionOperation.class);

	/**
	 * Mock {@link RetrieveHttpSessionOperation}.
	 */
	private final RetrieveHttpSessionOperation retrieveOperation = this
			.createMock(RetrieveHttpSessionOperation.class);

	/**
	 * Mock {@link InvalidateHttpSessionOperation}.
	 */
	private final InvalidateHttpSessionOperation invalidateOperation = this
			.createMock(InvalidateHttpSessionOperation.class);

	/**
	 * Ensure able to create, store and then retrieve the {@link HttpSession}.
	 */
	public void testCreateStoreRetrieve() {

		// Create the memory store
		HttpSessionStore store = new MemoryHttpSessionStore(10);

		// Values for testing
		final String SESSION_ID = "SESSION_ID";
		final long CREATION_TIME = 100;
		final long EXPIRE_TIME = Long.MAX_VALUE;
		final Map<String, Serializable> attributes = new HashMap<String, Serializable>();
		attributes.put("TEST", "VALUE");

		// Record
		this.recordReturn(this.createOperation,
				this.createOperation.getSessionId(), SESSION_ID);
		this.createOperation.sessionCreated(CREATION_TIME, EXPIRE_TIME,
				attributes);
		this.control(this.createOperation).setMatcher(
				new TypeMatcher(Long.class, Long.class, Map.class));
		this.recordReturn(this.storeOperation,
				this.storeOperation.getSessionId(), SESSION_ID);
		this.recordReturn(this.storeOperation,
				this.storeOperation.getCreationTime(), CREATION_TIME);
		this.recordReturn(this.storeOperation,
				this.storeOperation.getExpireTime(), EXPIRE_TIME);
		this.recordReturn(this.storeOperation,
				this.storeOperation.getAttributes(), attributes);
		this.storeOperation.sessionStored();
		this.recordReturn(this.retrieveOperation,
				this.retrieveOperation.getSessionId(), SESSION_ID);
		this.retrieveOperation.sessionRetrieved(CREATION_TIME, EXPIRE_TIME,
				attributes);

		// Test create, store, retrieve
		this.replayMockObjects();
		store.createHttpSession(this.createOperation);
		store.storeHttpSession(this.storeOperation);
		store.retrieveHttpSession(this.retrieveOperation);
		this.verifyMockObjects();

		// Ensure have correct attributes
		assertEquals("Incorrect attributes", "VALUE", attributes.get("TEST"));
	}

	/**
	 * Ensures {@link HttpSession} no longer available after invalidating it.
	 */
	public void testCreateInvalidate() {

		// Create the memory store
		HttpSessionStore store = new MemoryHttpSessionStore(10);

		// Values for testing
		final String SESSION_ID = "SESSION_ID";
		final long CREATION_TIME = 100;
		final long EXPIRE_TIME = Long.MAX_VALUE;
		final Map<String, Serializable> attributes = new HashMap<String, Serializable>();
		attributes.put("TEST", "VALUE");

		// Record
		this.recordReturn(this.createOperation,
				this.createOperation.getSessionId(), SESSION_ID);
		this.createOperation.sessionCreated(CREATION_TIME, EXPIRE_TIME,
				attributes);
		this.control(this.createOperation).setMatcher(
				new TypeMatcher(Long.class, Long.class, Map.class));
		this.recordReturn(this.storeOperation,
				this.storeOperation.getSessionId(), SESSION_ID);
		this.recordReturn(this.storeOperation,
				this.storeOperation.getCreationTime(), CREATION_TIME);
		this.recordReturn(this.storeOperation,
				this.storeOperation.getExpireTime(), EXPIRE_TIME);
		this.recordReturn(this.storeOperation,
				this.storeOperation.getAttributes(), attributes);
		this.storeOperation.sessionStored();
		this.recordReturn(this.invalidateOperation,
				this.invalidateOperation.getSessionId(), SESSION_ID);
		this.invalidateOperation.sessionInvalidated();
		this.recordReturn(this.retrieveOperation,
				this.retrieveOperation.getSessionId(), SESSION_ID);
		this.retrieveOperation.sessionNotAvailable();

		// Test create, invalidate, can not retrieve
		this.replayMockObjects();
		store.createHttpSession(this.createOperation);
		store.storeHttpSession(this.storeOperation); // Ensure created
		store.invalidateHttpSession(this.invalidateOperation);
		store.retrieveHttpSession(this.retrieveOperation);
		this.verifyMockObjects();
	}

	/**
	 * Ensures {@link HttpSession} is expired after being idle for maximum
	 * amount of time.
	 */
	public void testCreateExpire() {

		// Create the memory store (expires sessions immediately)
		HttpSessionStore store = new MemoryHttpSessionStore(0);

		// Values for testing
		final String SESSION_ID = "SESSION_ID";

		// Record
		this.recordReturn(this.createOperation,
				this.createOperation.getSessionId(), SESSION_ID);
		this.createOperation.sessionCreated(-1, -1, null);
		this.control(this.createOperation).setMatcher(
				new TypeMatcher(Long.class, Long.class, Map.class));
		this.recordReturn(this.createOperation,
				this.createOperation.getSessionId(), SESSION_ID);
		this.createOperation.sessionCreated(-1, -1, null); // same matcher
		this.recordReturn(this.retrieveOperation,
				this.retrieveOperation.getSessionId(), "ANOTHER_SESSION");
		this.retrieveOperation.sessionNotAvailable();

		// Test create, expire, can not retrieve
		this.replayMockObjects();
		store.createHttpSession(this.createOperation);
		store.createHttpSession(this.createOperation); // triggers expiring
		store.retrieveHttpSession(this.retrieveOperation);
		this.verifyMockObjects();
	}

}