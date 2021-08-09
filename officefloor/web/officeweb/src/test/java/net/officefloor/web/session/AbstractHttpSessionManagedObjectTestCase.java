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

import java.io.Serializable;
import java.net.HttpCookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockManagedObjectContext;
import net.officefloor.web.session.spi.CreateHttpSessionOperation;
import net.officefloor.web.session.spi.FreshHttpSession;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;
import net.officefloor.web.session.spi.HttpSessionStore;
import net.officefloor.web.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.web.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.web.session.spi.StoreHttpSessionOperation;

/**
 * Tests the {@link HttpSessionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSessionManagedObjectTestCase extends OfficeFrameTestCase {

	/**
	 * Name of {@link HttpCookie} to contain the Session Id.
	 */
	protected static final String SESSION_ID_COOKIE_NAME = "jsessionid";

	/**
	 * Marked current time for testing.
	 */
	protected static final Instant MOCK_CURRENT_TIME = Instant.now();

	/**
	 * Mock {@link Clock}.
	 */
	protected static final Clock clock = new Clock() {
		@Override
		public Clock withZone(ZoneId zone) {
			fail("Should not require changing the zone");
			return null;
		}

		@Override
		public Instant instant() {
			return MOCK_CURRENT_TIME;
		}

		@Override
		public ZoneId getZone() {
			return ZoneId.of("GMT");
		}
	};

	/**
	 * Index of the {@link ServerHttpConnection}.
	 */
	private final int serverHttpConnectionIndex = 0;

	/**
	 * Mock {@link AsynchronousContext}.
	 */
	protected final AsynchronousContext asynchronousContext = this.createMock(AsynchronousContext.class);

	/**
	 * Mock operations for the {@link MockHttpSessionIdGenerator} and
	 * {@link MockHttpSessionStore}.
	 */
	private final Deque<MockOperation> mockOperations = new LinkedList<MockOperation>();

	/**
	 * {@link FreshHttpSession}.
	 */
	protected FreshHttpSession freshHttpSession = null;

	/**
	 * {@link CreateHttpSessionOperation}.
	 */
	protected CreateHttpSessionOperation createOperation = null;

	/**
	 * {@link RetrieveHttpSessionOperation}.
	 */
	protected RetrieveHttpSessionOperation retrieveOperation = null;

	/**
	 * {@link StoreHttpSessionOperation}.
	 */
	protected StoreHttpSessionOperation storeOperation = null;

	/**
	 * {@link InvalidateHttpSessionOperation}.
	 */
	protected InvalidateHttpSessionOperation invalidateOperation = null;

	/**
	 * Creates the attributes.
	 * 
	 * @return Attributes.
	 */
	protected static Map<String, Serializable> newAttributes() {
		Map<String, Serializable> attributes = new HashMap<String, Serializable>();
		attributes.put("_TEST", "_ATTRIBUTE");
		return attributes;
	}

	/**
	 * Asserts the correctness of the {@link HttpSession}.
	 * 
	 * @param session Actual {@link HttpSession}.
	 */
	protected static void assertHttpSession(String sessionId, Instant creationTime, boolean isNew,
			HttpSession session) {
		assertEquals("Incorrect Session Id", sessionId, session.getSessionId());
		assertEquals("Incorrect creation time", creationTime, session.getCreationTime());
		assertEquals("Incorrect flagged on whether new", isNew, session.isNew());
		assertEquals("Incorrect underlying attributes", "_ATTRIBUTE", session.getAttribute("_TEST"));
	}

	/*
	 * =================== Record methods ===================================
	 */

	/**
	 * Records delay in operation.
	 */
	protected void record_delay() {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				// Do nothing
			}
		});
	}

	/**
	 * Records specifying the Session Id.
	 * 
	 * @param sessionId Session Id.
	 */
	protected void record_generate_setSessionId(final String sessionId) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.freshHttpSession.setSessionId(sessionId);
			}
		});
	}

	/**
	 * Records failing to generate the Session Id.
	 * 
	 * @param cause Cause of the failure.
	 */
	protected void record_generate_failedToGenerateSessionId(final Throwable cause) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.freshHttpSession.failedToGenerateSessionId(cause);
			}
		});
	}

	/**
	 * Records creating the {@link HttpSession}.
	 * 
	 * @param creationTime Creation time.
	 * @param expireTime   Time the {@link HttpSession} will expire if idle.
	 * @param attributes   Attributes.
	 */
	protected void record_create_sessionCreated(final Instant creationTime, final Instant expireTime,
			final Map<String, Serializable> attributes) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.createOperation.sessionCreated(creationTime, expireTime, attributes);
			}
		});
	}

	/**
	 * Records collision of Session Id.
	 */
	protected void record_create_sessionIdCollision() {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.createOperation.sessionIdCollision();
			}
		});
	}

	/**
	 * Records failing to create the {@link HttpSession}.
	 * 
	 * @param cause Cause of the failure.
	 */
	protected void record_create_failedToCreateSession(final Throwable cause) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.createOperation.failedToCreateSession(cause);
			}
		});
	}

	/**
	 * Records retrieving the {@link HttpSession}.
	 * 
	 * @param creationTime Creation time.
	 * @param expireTime   Time the {@link HttpSession} will expire if idle.
	 * @param attributes   Attributes.
	 */
	protected void record_retrieve_sessionRetrieved(final Instant creationTime, final Instant expireTime,
			final Map<String, Serializable> attributes) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.retrieveOperation.sessionRetrieved(creationTime, expireTime, attributes);
			}
		});
	}

	/**
	 * Records the {@link HttpSession} not being available.
	 */
	protected void record_retrieve_sessionNotAvailable() {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.retrieveOperation.sessionNotAvailable();
			}
		});
	}

	/**
	 * Records failing to retrieve the {@link HttpSession}.
	 * 
	 * @param cause Cause of the failure.
	 */
	protected void record_retrieve_failedToRetrieveSession(final Throwable cause) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.retrieveOperation.failedToRetreiveSession(cause);
			}
		});
	}

	/**
	 * Records {@link HttpSession} being stored.
	 */
	protected void record_store_sessionStored() {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.storeOperation.sessionStored();
			}
		});
	}

	/**
	 * Records failing to store the {@link HttpSession}.
	 * 
	 * @param cause Cause of the failure.
	 */
	protected void record_store_failedToStoreSession(final Throwable cause) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.storeOperation.failedToStoreSession(cause);
			}
		});
	}

	/**
	 * Records {@link HttpSession} being invalidated.
	 */
	protected void record_invalidate_sessionInvalidated() {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.invalidateOperation.sessionInvalidated();
			}
		});
	}

	/**
	 * Records failing to invalidate the {@link HttpSession}.
	 * 
	 * @param cause Cause of failure.
	 */
	protected void record_invalidate_failedToInvalidateSession(final Throwable cause) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.invalidateOperation.failedToInvalidateSession(cause);
			}
		});
	}

	/*
	 * ==================== Test run methods ============================
	 */

	/**
	 * Creates the {@link HttpSessionManagedObject}.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 * @return New {@link HttpSessionManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	protected HttpSessionManagedObject createHttpSessionManagedObject(ServerHttpConnection connection) {

		// Create the managed object (with mock dependencies)
		HttpSessionManagedObject mo = new HttpSessionManagedObject(SESSION_ID_COOKIE_NAME,
				this.serverHttpConnectionIndex, -1, new MockHttpSessionIdGenerator(), -1, new MockHttpSessionStore());

		// Load the managed object
		mo.setManagedObjectContext(new MockManagedObjectContext());
		mo.setAsynchronousContext(this.asynchronousContext);
		ManagedObjectUserStandAlone registry = new ManagedObjectUserStandAlone();
		registry.mapDependency(this.serverHttpConnectionIndex, connection);
		try {
			mo.loadObjects(registry);
		} catch (Throwable ex) {
			throw fail(ex);
		}

		// Return the managed object
		return mo;
	}

	/**
	 * Verifies the mock objects and all operations were completed.
	 */
	protected void verifyOperations() {
		this.verifyMockObjects();
		assertEquals("Operations still outstanding", 0, this.mockOperations.size());
	}

	/**
	 * Runs the next {@link MockOperation}.
	 * 
	 * @param session    {@link FreshHttpSession}.
	 * @param create     {@link CreateHttpSessionOperation}.
	 * @param retrieve   {@link RetrieveHttpSessionOperation}.
	 * @param store      {@link StoreHttpSessionOperation}.
	 * @param invalidate {@link InvalidateHttpSessionOperation}.
	 */
	private void runNextMockOperation(FreshHttpSession session, CreateHttpSessionOperation create,
			RetrieveHttpSessionOperation retrieve, StoreHttpSessionOperation store,
			InvalidateHttpSessionOperation invalidate) {
		// Obtain the next operation
		MockOperation operation = this.mockOperations.remove();

		// Load items for delay execution
		this.freshHttpSession = session;
		this.createOperation = create;
		this.retrieveOperation = retrieve;
		this.storeOperation = store;
		this.invalidateOperation = invalidate;

		// Load items for operation
		operation.freshHttpSession = session;
		operation.createOperation = create;
		operation.retrieveOperation = retrieve;
		operation.storeOperation = store;
		operation.invalidateOperation = invalidate;

		// Run the operation
		operation.run();
	}

	/**
	 * Mock {@link HttpSessionIdGenerator}.
	 */
	private class MockHttpSessionIdGenerator implements HttpSessionIdGenerator {

		/*
		 * ============ HttpSessionIdGenerator ========================
		 */

		@Override
		public void generateSessionId(FreshHttpSession session) {
			AbstractHttpSessionManagedObjectTestCase.this.runNextMockOperation(session, null, null, null, null);
		}
	}

	/**
	 * Mock {@link HttpSessionStore}.
	 */
	private class MockHttpSessionStore implements HttpSessionStore {

		/*
		 * ================= HttpSessionStore ==========================
		 */

		@Override
		public void createHttpSession(CreateHttpSessionOperation operation) {
			AbstractHttpSessionManagedObjectTestCase.this.runNextMockOperation(null, operation, null, null, null);
		}

		@Override
		public void retrieveHttpSession(RetrieveHttpSessionOperation operation) {
			AbstractHttpSessionManagedObjectTestCase.this.runNextMockOperation(null, null, operation, null, null);
		}

		@Override
		public void storeHttpSession(StoreHttpSessionOperation operation) {
			AbstractHttpSessionManagedObjectTestCase.this.runNextMockOperation(null, null, null, operation, null);
		}

		@Override
		public void invalidateHttpSession(InvalidateHttpSessionOperation operation) {
			AbstractHttpSessionManagedObjectTestCase.this.runNextMockOperation(null, null, null, null, operation);
		}
	}

	/**
	 * Operation on the mock object.
	 */
	private abstract class MockOperation implements Runnable {

		/**
		 * {@link FreshHttpSession}.
		 */
		public FreshHttpSession freshHttpSession = null;

		/**
		 * {@link CreateHttpSessionOperation}.
		 */
		public CreateHttpSessionOperation createOperation = null;

		/**
		 * {@link RetrieveHttpSessionOperation}.
		 */
		public RetrieveHttpSessionOperation retrieveOperation = null;

		/**
		 * {@link StoreHttpSessionOperation}.
		 */
		public StoreHttpSessionOperation storeOperation = null;

		/**
		 * {@link InvalidateHttpSessionOperation}.
		 */
		public InvalidateHttpSessionOperation invalidateOperation = null;
	}

}
