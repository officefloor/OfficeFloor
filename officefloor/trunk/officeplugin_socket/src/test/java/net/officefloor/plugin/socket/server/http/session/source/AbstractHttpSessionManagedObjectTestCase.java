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

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.spi.CreateHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.FreshHttpSession;
import net.officefloor.plugin.socket.server.http.session.spi.HttpSessionIdGenerator;
import net.officefloor.plugin.socket.server.http.session.spi.HttpSessionStore;
import net.officefloor.plugin.socket.server.http.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.StoreHttpSessionOperation;

/**
 * Tests the {@link HttpSessionManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSessionManagedObjectTestCase extends
		OfficeFrameTestCase {

	/**
	 * Name of {@link HttpCookie} to contain the Session Id.
	 */
	private static final String SESSION_ID_COOKIE_NAME = "jsessionid";

	/**
	 * Index of the {@link ServerHttpConnection}.
	 */
	private final int serverHttpConnectionIndex = 0;

	/**
	 * Index of the {@link HttpSessionIdGenerator}.
	 */
	private final int sessionIdGeneratorIndex = 1;

	/**
	 * Index of the {@link HttpSessionStore}.
	 */
	private final int sessionStoreIndex = 2;

	/**
	 * Mock {@link AsynchronousListener}.
	 */
	protected final AsynchronousListener asynchronousListener = this
			.createMock(AsynchronousListener.class);

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<Indexed> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * Creates the {@link HttpSessionManagedObject}.
	 *
	 * @param generator
	 *            {@link HttpSessionIdGenerator}.
	 * @param store
	 *            {@link HttpSessionStore}.
	 * @return New {@link HttpSessionManagedObject}.
	 */
	protected HttpSessionManagedObject createHttpSessionManagedObject(
			HttpSessionIdGenerator generator, HttpSessionStore store) {
		return new HttpSessionManagedObject(SESSION_ID_COOKIE_NAME,
				this.serverHttpConnectionIndex, this.sessionIdGeneratorIndex,
				generator, this.sessionStoreIndex, store);
	}

	/**
	 * Creates the attributes.
	 *
	 * @return Attributes.
	 */
	protected static Map<String, Object> newAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("_TEST", "_ATTRIBUTE");
		return attributes;
	}

	/**
	 * Asserts the correctness of the {@link HttpSession}.
	 *
	 * @param session
	 *            Actual {@link HttpSession}.
	 */
	protected static void assertHttpSession(String sessionId,
			long creationTime, boolean isNew, HttpSession session) {
		assertEquals("Incorrect Session Id", sessionId, session.getSessionId());
		assertEquals("Incorrect creation time", creationTime, session
				.getCreationTime());
		assertEquals("Incorrect flagged on whether new", isNew, session.isNew());
		assertEquals("Incorrect underlying attributes", "_ATTRIBUTE", session
				.getAttribute("_TEST"));
	}

	/**
	 * Records obtaining the {@link HttpRequest}.
	 */
	protected void record_obtainHttpRequest() {
		this.recordReturn(this.objectRegistry, this.objectRegistry
				.getObject(this.serverHttpConnectionIndex), this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
	}

	/**
	 * Records obtaining the {@link HttpSessionIdGenerator}.
	 *
	 * @param generator
	 *            {@link HttpSessionIdGenerator} to return.
	 */
	protected void record_obtainHttpSessionIdGenerator(
			HttpSessionIdGenerator generator) {
		this.recordReturn(this.objectRegistry, this.objectRegistry
				.getObject(this.sessionIdGeneratorIndex), generator);
	}

	/**
	 * Records obtaining the Session Id {@link HttpCookie}.
	 *
	 * @param sessionId
	 *            <code>null</code> indicates no Session Id {@link HttpCookie},
	 *            while a value will have the {@link HttpCookie} available.
	 */
	protected void record_obtainSessionIdCookie(String sessionId) {
		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		if (sessionId != null) {
			headers.add(new HttpHeaderImpl("cookie", SESSION_ID_COOKIE_NAME
					+ "=" + sessionId));
		}
		this.recordReturn(this.request, this.request.getHeaders(), headers);
	}

	/**
	 * Records obtaining the {@link HttpSessionStore}.
	 *
	 * @param store
	 *            {@link HttpSessionStore}.
	 */
	protected void record_obtainHttpSessionStore(HttpSessionStore store) {
		this.recordReturn(this.objectRegistry, this.objectRegistry
				.getObject(this.sessionStoreIndex), store);
	}

	/**
	 * Triggers starting the coordination.
	 *
	 * @param mo
	 *            {@link HttpSessionManagedObject}.
	 */
	protected void startCoordination(HttpSessionManagedObject mo)
			throws Throwable {

		// Register the asynchronous listener
		mo.registerAsynchronousCompletionListener(this.asynchronousListener);

		// Trigger the coordination
		mo.loadObjects(this.objectRegistry);
	}

	/**
	 * Mock {@link HttpSessionIdGenerator}.
	 */
	protected class MockHttpSessionIdGenerator implements
			HttpSessionIdGenerator {

		/**
		 * Session Id.
		 */
		private String sessionId;

		/**
		 * Failure.
		 */
		private Throwable failure;

		/**
		 * {@link FreshHttpSession}.
		 */
		private FreshHttpSession session = null;

		/**
		 * Initiate for asynchronous generation.
		 */
		public MockHttpSessionIdGenerator() {
		}

		/**
		 * Convenience constructor returning session Id immediately.
		 *
		 * @param sessionId
		 *            Session Id to be generated.
		 */
		public MockHttpSessionIdGenerator(String sessionId) {
			this.sessionId = sessionId;
		}

		/**
		 * Specifies the session Id.
		 *
		 * @param sessionId
		 *            Session Id.
		 */
		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
			if (this.session != null) {
				this.session.setSessionId(this.sessionId);
			}
		}

		/**
		 * Specifies failure in generating.
		 *
		 * @param failure
		 *            Failure.
		 */
		public void setFailure(Throwable failure) {
			this.failure = failure;
			if (this.session != null) {
				this.session.failedToGenerateSessionId(this.failure);
			}
		}

		/*
		 * ============ HttpSessionIdGenerator ========================
		 */

		@Override
		public void generateSessionId(FreshHttpSession session) {
			this.session = session;

			// Handle failure
			if (this.failure != null) {
				session.failedToGenerateSessionId(this.failure);
				return;
			}

			// Handle generate session id
			if (this.sessionId != null) {
				session.setSessionId(this.sessionId);
				return;
			}

			// Otherwise taking time to generate
		}
	}

	/**
	 * Mock {@link HttpSessionStore}.
	 */
	protected class MockHttpSessionStore implements HttpSessionStore {

		/**
		 * Creation time.
		 */
		private final long creationTime;

		/**
		 * Failure.
		 */
		private Throwable failure = null;

		/**
		 * Flag indicating if collision of session Id.
		 */
		private boolean isCollision = false;

		/**
		 * Attributes.
		 */
		private Map<String, Object> attributes = null;

		/**
		 * {@link CreateHttpSessionOperation}.
		 */
		private CreateHttpSessionOperation create;

		/**
		 * {@link RetrieveHttpSessionOperation}.
		 */
		private RetrieveHttpSessionOperation retrieve;

		/**
		 * {@link StoreHttpSessionOperation}.
		 */
		private StoreHttpSessionOperation store;
		/**
		 * {@link InvalidateHttpSessionOperation}.
		 */
		private InvalidateHttpSessionOperation invalidate;

		/**
		 * Initiate.
		 *
		 * @param creationTime
		 *            Creation time.
		 */
		public MockHttpSessionStore(long creationTime) {
			this.creationTime = creationTime;
		}

		/**
		 * Convenience constructor creating/retrieving session immediately.
		 *
		 * @param creationTime
		 *            Creation time.
		 * @param attributes
		 *            Attributes.
		 */
		public MockHttpSessionStore(long creationTime,
				Map<String, Object> attributes) {
			this(creationTime);
			this.attributes = attributes;
		}

		/**
		 * Loads the attributes.
		 *
		 * @param attributes
		 *            Loaded the attributes
		 */
		public void loadAttributes(Map<String, Object> attributes) {
			this.attributes = attributes;

			// Handle if creation
			if (this.create != null) {
				this.create.sessionCreated(this.creationTime, attributes);
				return;
			}
		}

		/**
		 * Loads a failure.
		 *
		 * @param cause
		 *            Cause of the failure.
		 */
		public void loadFailure(Throwable cause) {
			this.failure = cause;

			// Handle if creation
			if (this.create != null) {
				this.create.failedToCreateSession(cause);
				return;
			}
		}

		/**
		 * Flags for Session Id collision.
		 */
		public void flagSessionIdCollision() {
			this.isCollision = true;

			// Handle if being created
			if (this.create != null) {
				this.isCollision = false; // collide only once
				this.create.sessionIdCollision();
				return;
			}
		}

		/*
		 * ================= HttpSessionStore ==========================
		 */

		@Override
		public void createHttpSession(CreateHttpSessionOperation operation) {
			this.create = operation;

			// Handle if failure
			if (this.failure != null) {
				this.create.failedToCreateSession(this.failure);
				return;
			}

			// Handle if collision
			if (this.isCollision) {
				this.isCollision = false; // collide only once
				this.create.sessionIdCollision();
				return;
			}

			// Handle if successful
			if (this.attributes != null) {
				this.create.sessionCreated(this.creationTime, this.attributes);
				return;
			}

			// Otherwise asynchronous creation
		}

		@Override
		public void retrieveHttpSession(RetrieveHttpSessionOperation operation) {
			this.retrieve = operation;
		}

		@Override
		public void storeHttpSession(StoreHttpSessionOperation operation) {
			this.store = operation;
		}

		@Override
		public void invalidateHttpSession(
				InvalidateHttpSessionOperation operation) {
			this.invalidate = operation;
		}
	}

}