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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.cookie.HttpCookie;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.HttpSessionManagedObject;
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
	 * Index of the {@link ServerHttpConnection}.
	 */
	private final int serverHttpConnectionIndex = 0;

	/**
	 * Mock {@link AsynchronousContext}.
	 */
	protected final AsynchronousContext asynchronousContext = this.createMock(AsynchronousContext.class);

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<Indexed> objectRegistry = this.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

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
	 * @param session
	 *            Actual {@link HttpSession}.
	 */
	protected static void assertHttpSession(String sessionId, long creationTime, boolean isNew, HttpSession session) {
		assertEquals("Incorrect Session Id", sessionId, session.getSessionId());
		assertEquals("Incorrect creation time", creationTime, session.getCreationTime());
		assertEquals("Incorrect flagged on whether new", isNew, session.isNew());
		assertEquals("Incorrect underlying attributes", "_ATTRIBUTE", session.getAttribute("_TEST"));
	}

	/*
	 * =================== Record methods ===================================
	 */

	/**
	 * Records obtaining the {@link HttpRequest} and subsequently the Session Id
	 * {@link HttpCookie}.
	 * 
	 * @param sessionId
	 *            <code>null</code> indicates no Session Id {@link HttpCookie},
	 *            while a value will have the {@link HttpCookie} available.
	 */
	protected void record_sessionIdCookie(String sessionId) {
		// Record obtaining the Http Request
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(this.serverHttpConnectionIndex),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(), this.request);

		// Record obtaining the Session Id
		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		if (sessionId != null) {
			headers.add(createHttpHeader("cookie", SESSION_ID_COOKIE_NAME + "=" + sessionId));
		}
		this.recordReturn(this.request, this.request.getHttpHeaders(), headers);
	}

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
	 * @param sessionId
	 *            Session Id.
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
	 * @param cause
	 *            Cause of the failure.
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
	 * @param creationTime
	 *            Creation time.
	 * @param expireTime
	 *            Time the {@link HttpSession} will expire if idle.
	 * @param attributes
	 *            Attributes.
	 */
	protected void record_create_sessionCreated(final long creationTime, final long expireTime,
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
	 * @param cause
	 *            Cause of the failure.
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
	 * @param creationTime
	 *            Creation time.
	 * @param expireTime
	 *            Time the {@link HttpSession} will expire if idle.
	 * @param attributes
	 *            Attributes.
	 */
	protected void record_retrieve_sessionRetrieved(final long creationTime, final long expireTime,
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
	 * @param cause
	 *            Cause of the failure.
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
	 * @param cause
	 *            Cause of the failure.
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
	 * @param cause
	 *            Cause of failure.
	 */
	protected void record_invalidate_failedToInvalidateSession(final Throwable cause) {
		this.mockOperations.add(new MockOperation() {
			@Override
			public void run() {
				this.invalidateOperation.failedToInvalidateSession(cause);
			}
		});
	}

	/**
	 * Records adding a {@link HttpCookie} to the {@link HttpResponse}.
	 * 
	 * @param isExistingSessionCookie
	 *            Is the {@link HttpCookie} already on the response.
	 * @param sessionId
	 *            Session Id.
	 * @param expireTime
	 *            Time to expire the added {@link HttpCookie}.
	 */
	protected void record_cookie_addSessionId(boolean isExistingSessionCookie, String sessionId, long expireTime) {

		// Record checking for existing Session Id cookie
		this.recordReturn(this.connection, this.connection.getHttpResponse(), this.response);
		if (isExistingSessionCookie) {
			// Record existing Session cookie
			HttpHeader existingCookieHeader = createHttpHeader("set-cookie",
					SESSION_ID_COOKIE_NAME + "=\"Existing Session Id\"");
			this.recordReturn(this.response, this.response.getHttpHeaders(), new HttpHeader[] { existingCookieHeader });
			this.response.getHttpHeaders().removeHeader(existingCookieHeader);
		} else {
			// Record no existing Session cookie
			this.recordReturn(this.response, this.response.getHttpHeaders(), new HttpHeader[0]);
		}

		// Record adding the Session Id cookie
		HttpCookie cookie = new HttpCookie(SESSION_ID_COOKIE_NAME, sessionId);
		cookie.setExpires(expireTime);
		cookie.setPath("/");
		this.recordReturn(this.response,
				this.response.getHttpHeaders().addHeader("set-cookie", cookie.toHttpResponseHeaderValue()),
				this.createMock(HttpHeader.class));
	}

	/*
	 * ==================== Test run methods ============================
	 */

	/**
	 * Creates the {@link HttpSessionManagedObject}.
	 * 
	 * @return New {@link HttpSessionManagedObject}.
	 */
	protected HttpSessionManagedObject createHttpSessionManagedObject() {
		return new HttpSessionManagedObject(SESSION_ID_COOKIE_NAME, this.serverHttpConnectionIndex, -1,
				new MockHttpSessionIdGenerator(), -1, new MockHttpSessionStore());
	}

	/**
	 * Triggers running the coordination.
	 * 
	 * @param mo
	 *            {@link HttpSessionManagedObject}.
	 */
	protected void startCoordination(HttpSessionManagedObject mo) throws Throwable {

		// Register the asynchronous context
		mo.setAsynchronousContext(this.asynchronousContext);

		// Trigger the coordination
		mo.loadObjects(this.objectRegistry);
	}

	/**
	 * Verifies the mock objects and all operations were completed.
	 */
	protected void verifyOperations() {
		this.verifyMockObjects();
		assertEquals("Operations still outstanding", 0, this.mockOperations.size());
	}

	/**
	 * Creates a mock {@link HttpHeader}.
	 * 
	 * @param name
	 *            {@link HttpHeader} name.
	 * @param value
	 *            {@link HttpHeader} value.
	 * @return Mock {@link HttpHeader}.
	 */
	private static HttpHeader createHttpHeader(String name, String value) {
		return MockHttpServer.mockRequest().header(name, value).build().getHttpHeaders().getHeader(name);
	}

	/**
	 * Runs the next {@link MockOperation}.
	 * 
	 * @param session
	 *            {@link FreshHttpSession}.
	 * @param create
	 *            {@link CreateHttpSessionOperation}.
	 * @param retrieve
	 *            {@link RetrieveHttpSessionOperation}.
	 * @param store
	 *            {@link StoreHttpSessionOperation}.
	 * @param invalidate
	 *            {@link InvalidateHttpSessionOperation}.
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